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

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

  public Query buildSearchQuery(ElasticSlotSearchRequest request) {
    List<Query> must = new ArrayList<>();
    addDateTimeFilters(must, request);
    addOptionalFilters(must, request);
    return Query.of(q -> q.bool(BoolQuery.of(b -> b.must(must))));
  }

  private void addDateTimeFilters(List<Query> must, ElasticSlotSearchRequest req) {
    must.add(Query.of(q -> q.range(r -> r.date(d -> d.field("date")
        .gte(req.dateFrom().format(DateTimeFormatter.ISO_LOCAL_DATE))
        .lte(req.dateTo().format(DateTimeFormatter.ISO_LOCAL_DATE))))));
    if (req.timeFrom() != null) {
      String timeStr = req.timeFrom().format(TIME_FMT);
      must.add(Query.of(q -> q.range(r -> r.date(d -> d
          .field("timeFrom").lte(timeStr)))));
    }
    if (req.timeTo() != null) {
      String timeStr = req.timeTo().format(TIME_FMT);
      must.add(Query.of(q -> q.range(r -> r.date(d -> d
          .field("timeTo").gte(timeStr)))));
    }
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
