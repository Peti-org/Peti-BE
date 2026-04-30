package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Assembles {@link ElasticSlotDocument} instances from resolved capacity ranges,
 * caretaker domain entity, and service configuration.
 */
@Component
public class ElasticSlotAssembler {

  /**
   * Build the final list of slot documents for one caretaker on one day.
   *
   * @param date           the slot date
   * @param capacityRanges map of requiredCapacity → continuous time ranges
   * @param segments       ordered segments used to look up ServiceConfig per range
   * @param caretaker      domain entity — source of all caretaker data
   * @return assembled slot documents ready for indexing
   */
  public List<ElasticSlotDocument> assemble(
      LocalDate date,
      Map<Integer, List<TimeRange>> capacityRanges,
      List<TimeSegmentWithPricing> segments,
      Caretaker caretaker
  ) {
    List<ElasticSlotDocument> slots = new ArrayList<>();
    Instant now = Instant.now();
    User user = caretaker.getUserReference();

    for (Map.Entry<Integer, List<TimeRange>> entry : capacityRanges.entrySet()) {
      int capacity = entry.getKey();
      for (TimeRange range : entry.getValue()) {
        ServiceConfig serviceConfig = SlotRangeResolver.findServiceConfigForRange(segments, range);
        if (serviceConfig == null) {
          continue;
        }
        slots.add(buildDocument(date, capacity, range, serviceConfig, caretaker, user, now));
      }
    }

    return slots;
  }

  private ElasticSlotDocument buildDocument(
      LocalDate date,
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
        .caretakerPreferences(caretaker.getCaretakerPreference())
        .serviceConfig(serviceConfig)
        .date(date)
        .timeFrom(range.timeFrom())
        .timeTo(range.timeTo())
        .capacity(capacity)
        .createdAt(now)
        .updatedAt(now)
        .build();
  }
}

