package com.peti.backend.service.elastic.builder;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.TimeRange;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Assembles {@link ElasticSlotDocument} instances from resolved capacity ranges,
 * caretaker domain entity, and service configuration.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ElasticSlotAssembler {

  /**
   * Build the final list of slot documents for one caretaker on one day.
   *
   * @param capacityRanges map of requiredCapacity → continuous time ranges
   * @param caretaker      domain entity — source of all caretaker data
   * @return assembled slot documents ready for indexing
   */
  public static List<ElasticSlotDocument> assemble(
      Map<Integer, List<TimeRange>> capacityRanges,
      Caretaker caretaker,
      ServiceConfig serviceConfig
  ) {
    List<ElasticSlotDocument> slots = new ArrayList<>();
    Instant now = Instant.now();
    User user = caretaker.getUserReference();

    for (Map.Entry<Integer, List<TimeRange>> entry : capacityRanges.entrySet()) {
      int capacity = entry.getKey();
      for (TimeRange range : entry.getValue()) {
        slots.add(buildDocument(capacity, range, serviceConfig, caretaker, user, now));
      }
    }

    return slots;
  }

  private static ElasticSlotDocument buildDocument(
      int capacity,
      TimeRange range,
      ServiceConfig serviceConfig,
      Caretaker caretaker,
      User user,
      Instant now
  ) {
    String cityId = user.getCityByCityId() != null
        ? String.valueOf(user.getCityByCityId().getCityId()) : null;
    String cityName = user.getCityByCityId() != null
        ? user.getCityByCityId().getCity() : null;

    return ElasticSlotDocument.builder()
        .id(null)
        .caretakerId(caretaker.getCaretakerId().toString())
        .caretakerFirstName(user.getFirstName())
        .caretakerLastName(user.getLastName())
        .caretakerRating(caretaker.getRating())
        .caretakerCityId(cityId)
        .caretakerCityName(cityName)
        .serviceConfig(serviceConfig)
        .fromDateTime(range.timeFrom())
        .toDateTime(range.timeTo())
        .capacity(capacity)
        .createdAt(now)
        .build();
  }
}

