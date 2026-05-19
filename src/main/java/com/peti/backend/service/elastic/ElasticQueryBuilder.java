package com.peti.backend.service.elastic;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Builds Elasticsearch queries for slot search requests.
 *
 * <p>Filters by date, time, city, rating and capacity.
 * Pet-preference matching is done in Java after retrieval because
 * caretakerPreferences is stored as an opaque (non-indexed) object.
 */
@Component
public class ElasticQueryBuilder {

  public Query buildSearchQuery(ElasticSlotSearchRequest request) {
    List<Query> must = new ArrayList<>();
    addDateTimeFilters(must, request);
    addOptionalFilters(must, request);
    return Query.of(q -> q.bool(BoolQuery.of(b -> b.must(must))));
  }

  /**
   * Overlap logic: slot.fromDateTime <= requestEnd AND slot.toDateTime >= requestStart.
   * Uses fromDateTime/toDateTime fields as stored in the index.
   */
  private void addDateTimeFilters(List<Query> must, ElasticSlotSearchRequest req) {
    var timeFrom = req.timeFrom() != null ? req.timeFrom() : java.time.LocalTime.MIN;
    var timeTo = req.timeTo() != null ? req.timeTo() : java.time.LocalTime.of(23, 59, 59);

    String requestStart = req.dateFrom().atTime(timeFrom)
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    String requestEnd = req.dateTo().atTime(timeTo)
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

    // Slot starts before or at the requested end
    must.add(Query.of(q -> q.range(r -> r.date(d -> d
        .field("fromDateTime").lte(requestEnd)))));
    // Slot ends after or at the requested start
    must.add(Query.of(q -> q.range(r -> r.date(d -> d
        .field("toDateTime").gte(requestStart)))));
  }

  private void addOptionalFilters(List<Query> must, ElasticSlotSearchRequest req) {
    if (req.cityId() != null) {
      must.add(Query.of(q -> q.term(t -> t
          .field("caretakerCityId").value(FieldValue.of(req.cityId())))));
    }
    if (req.minRating() != null) {
      must.add(Query.of(q -> q.range(r -> r.number(n -> n
          .field("caretakerRating").gte((double) req.minRating())))));
    }
    int minCapacity = req.pets() != null ? req.pets().size() : 1;
    must.add(Query.of(q -> q.range(r -> r.number(n -> n
        .field("capacity").gte((double) minCapacity)))));
  }
}
