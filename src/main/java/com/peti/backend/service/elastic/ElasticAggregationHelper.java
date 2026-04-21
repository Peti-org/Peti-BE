package com.peti.backend.service.elastic;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TopHitsAggregation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

/**
 * Builds Elasticsearch aggregations and extracts grouped results for slot search.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ElasticAggregationHelper {

  private final ObjectMapper objectMapper;

  /** Build a terms-aggregation grouped by caretaker with a top-hit sub-aggregation. */
  public Aggregation buildCaretakerAggregation(int bucketSize) {
    TopHitsAggregation topHits = TopHitsAggregation.of(th -> th
        .size(1)
        .sort(s -> s.field(f -> f.field("capacity").order(SortOrder.Desc)))
    );

    return Aggregation.of(a -> a
        .terms(TermsAggregation.of(t -> t
            .field("caretakerId")
            .size(bucketSize)))
        .aggregations("best_slot", Aggregation.of(sa -> sa.topHits(topHits)))
    );
  }

  /** Extract the best slot per caretaker from aggregation results. */
  public List<ElasticSlotDocument> extractBestSlots(SearchHits<ElasticSlotDocument> searchHits) {
    List<ElasticSlotDocument> bestSlots = new ArrayList<>();

    if (!searchHits.hasAggregations()) {
      return bestSlots;
    }

    var container = searchHits.getAggregations();
    if (!(container instanceof ElasticsearchAggregations esAggs)) {
      return bestSlots;
    }

    var caretakersWrapper = esAggs.aggregationsAsMap().get("caretakers");
    if (caretakersWrapper == null) {
      return bestSlots;
    }

    var aggregate = caretakersWrapper.aggregation().getAggregate();
    if (!aggregate.isSterms()) {
      return bestSlots;
    }

    for (var bucket : aggregate.sterms().buckets().array()) {
      var bestSlotAgg = bucket.aggregations().get("best_slot");
      if (bestSlotAgg == null || !bestSlotAgg.isTopHits()) {
        continue;
      }

      var hits = bestSlotAgg.topHits().hits().hits();
      if (hits.isEmpty()) {
        continue;
      }

      var hit = hits.getFirst();
      if (hit.source() == null) {
        continue;
      }

      try {
        String json = hit.source().toJson().toString();
        ElasticSlotDocument slot = objectMapper.readValue(json, ElasticSlotDocument.class);
        slot.setId(hit.id());
        bestSlots.add(slot);
      } catch (Exception e) {
        log.warn("Failed to deserialize slot from aggregation hit: {}", e.getMessage());
      }
    }

    return bestSlots;
  }
}
