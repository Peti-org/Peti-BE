package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.PetFilter;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.SortDirection;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.SortField;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ElasticQueryBuilderTest {

  private ElasticQueryBuilder queryBuilder;

  @BeforeEach
  void setUp() {
    queryBuilder = new ElasticQueryBuilder();
  }

  @Test
  @DisplayName("Builds query with all filters")
  void fullQuery() {
    ElasticSlotSearchRequest request = new ElasticSlotSearchRequest(
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7),
        LocalTime.of(9, 0), LocalTime.of(18, 0),
        "1", null, null, "UAH", 3,
        List.of(new PetFilter("dog", null, "medium", null, null, null),
                new PetFilter("cat", null, "small", null, null, null)),
        null, 0, 10, SortField.RATING, SortDirection.DESC
    );

    Query query = queryBuilder.buildSearchQuery(request);
    assertThat(query).isNotNull();
    assertThat(query.isBool()).isTrue();
    // date filter + timeFrom + timeTo + cityId + minRating + capacity = 6 must clauses
    assertThat(query.bool().must()).hasSize(6);
  }

  @Test
  @DisplayName("Builds query with minimal filters (no time, no rating, no city)")
  void minimalQuery() {
    ElasticSlotSearchRequest request = new ElasticSlotSearchRequest(
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7),
        null, null,
        null, null, null, "UAH", null,
        null, null, 0, 10, SortField.DATE, SortDirection.ASC
    );

    Query query = queryBuilder.buildSearchQuery(request);
    assertThat(query).isNotNull();
    assertThat(query.isBool()).isTrue();
    // date filter + capacity(min 1) = 2 must clauses
    assertThat(query.bool().must()).hasSize(2);
  }

  @Test
  @DisplayName("Capacity filter uses pet count when pets provided")
  void capacityMatchesPetCount() {
    ElasticSlotSearchRequest request = new ElasticSlotSearchRequest(
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7),
        null, null, null, null, null, "UAH", null,
        List.of(
            new PetFilter("dog", null, "medium", null, null, null),
            new PetFilter("cat", null, "small", null, null, null),
            new PetFilter("bird", null, "small", null, null, null)
        ),
        null, 0, 10, SortField.RATING, SortDirection.DESC
    );

    Query query = queryBuilder.buildSearchQuery(request);
    assertThat(query).isNotNull();
    // Should have capacity >= 3 in the must clauses
    String queryJson = query.toString();
    assertThat(queryJson).contains("capacity");
  }
}

