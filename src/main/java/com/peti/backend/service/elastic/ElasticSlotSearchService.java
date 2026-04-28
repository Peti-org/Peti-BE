package com.peti.backend.service.elastic;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchResponse;
import com.peti.backend.dto.elastic.SlotSearchResult;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.PetInfo;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

/**
 * Searches for available slots using Elasticsearch aggregations,
 * picking the best slot per caretaker and calculating prices.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticSlotSearchService {

  private final ElasticsearchOperations elasticsearchOperations;
  private final ElasticQueryBuilder queryBuilder;
  private final ElasticAggregationHelper aggregationHelper;
  private final SlotSearchResultMapper resultMapper;

  /** Search for available slots, picking the best slot per caretaker. */
  public ElasticSlotSearchResponse searchSlots(ElasticSlotSearchRequest rawRequest) {
    ElasticSlotSearchRequest request = rawRequest.withDefaults();
    log.debug("Searching slots with request: {}", request);

    Query query = queryBuilder.buildSearchQuery(request);
    int bucketsToFetch = (request.page() + 3) * request.pageSize();

    NativeQuery searchQuery = NativeQuery.builder()
        .withQuery(query)
        .withAggregation("caretakers", aggregationHelper.buildCaretakerAggregation(bucketsToFetch))
        .withMaxResults(0)
        .build();

    SearchHits<ElasticSlotDocument> searchHits =
        elasticsearchOperations.search(searchQuery, ElasticSlotDocument.class);

    List<ElasticSlotDocument> bestSlots = aggregationHelper.extractBestSlots(searchHits);
    log.debug("Found {} best slots from aggregations", bestSlots.size());

    List<PetInfo> petInfos = resolvePetInfos(request);

    List<SlotSearchResult> results = bestSlots.stream()
        .map(slot -> resultMapper.toSearchResult(slot, petInfos))
        .filter(r -> r.priceBreakdown() != null)
        .sorted(resultMapper.getComparator(request))
        .collect(Collectors.toList());

    int start = request.page() * request.pageSize();
    int end = Math.min(start + request.pageSize(), results.size());
    List<SlotSearchResult> page = start < results.size() ? results.subList(start, end) : List.of();

    return ElasticSlotSearchResponse.of(page, request.page(), request.pageSize(), results.size());
  }

  private List<PetInfo> resolvePetInfos(ElasticSlotSearchRequest request) {
    return request.pets() != null
        ? request.pets().stream().map(p -> new PetInfo(p.animalType(), p.size())).toList()
        : List.of(new PetInfo("dog", "medium"));
  }
}
