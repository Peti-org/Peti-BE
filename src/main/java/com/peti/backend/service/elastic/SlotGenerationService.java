package com.peti.backend.service.elastic;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import com.peti.backend.model.internal.ServiceType;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.peti.backend.service.elastic.builder.CapacityTimelineBuilder;
import com.peti.backend.service.elastic.builder.ElasticSlotAssembler;
import com.peti.backend.service.elastic.builder.SlotRangeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Orchestrates capacity-layered slot generation for a single caretaker on a single day.
 *
 * <p>Supports multiple overlapping RRules — capacities are SUMMED in overlapping windows.
 *
 * <p>Example with 2 RRules:
 * <ul>
 *   <li>RRule1: 08:00–20:00, capacity 3</li>
 *   <li>RRule2: 18:00–22:00, capacity 2</li>
 *   <li>Result: 08–18 (cap 3), 18–20 (cap 5), 20–22 (cap 2)</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlotGenerationService {

  /**
   * Generate all capacity-layered slot documents for a single caretaker on a single day.
   *
   * @param date      date to generate slots for
   * @param rrules    availability rules active on that day (must share the same caretaker)
   * @param bookings  existing bookings that reduce available capacity
   * @param caretaker the caretaker domain entity (must have userReference loaded)
   * @return list of generated slot documents ready for indexing
   */
  public List<ElasticSlotDocument> generateSlotsForDay(LocalDate date, List<CaretakerRRule> rrules,
      List<BookingInput> bookings, Caretaker caretaker) {
    List<CaretakerRRule> validRules = filterValidRules(rrules);
    if (validRules.isEmpty()) {
      return List.of();
    }

    if (isEmpty(caretaker.getCaretakerPreference()) || caretaker.getCaretakerPreference().services() == null) {
      return List.of();
    }

    // Each type of slot should be generated separately because if two slots with different type overlap
    // we need to generate two separate segments for each type
    Map<String, List<CaretakerRRule>> rulesByType = validRules.stream().collect(Collectors.groupingBy(
        CaretakerRRule::getSlotType, Collectors.toList()));

    return rulesByType.values().stream()
        .map(rules -> buildSlotsForSpecificType(date, rules, bookings, caretaker))
        .flatMap(List::stream)
        .toList();
  }

  private List<ElasticSlotDocument> buildSlotsForSpecificType(LocalDate date, List<CaretakerRRule> rrules,
      List<BookingInput> bookings, Caretaker caretaker) {
    //sort rrules by type and build for each slot type separately segments
    String slotType = rrules.getFirst().getSlotType();
    ServiceConfig serviceConfig = caretaker.getCaretakerPreference().services().stream()
        .filter(config -> config.type().equals(ServiceType.fromName(slotType)))
        .findFirst()
        .orElse(null);

    if (serviceConfig == null) {
      return List.of();
    }

    List<TimeSegmentWithPricing> segments = CapacityTimelineBuilder.buildSegments(rrules, bookings, date);
    Map<Integer, List<TimeRange>> capacityRanges = SlotRangeResolver.resolveRangesByCapacity(segments);
    return ElasticSlotAssembler.assemble(capacityRanges, caretaker, serviceConfig);
  }

  /**
   * Filter out disabled rules and rules with invalid time ranges or zero capacity.
   */
  private List<CaretakerRRule> filterValidRules(List<CaretakerRRule> rrules) {
    if (rrules == null || rrules.isEmpty()) {
      return List.of();
    }
    return rrules.stream()
        .filter(r -> Boolean.TRUE.equals(r.getIsEnabled()))
        .filter(r -> r.getCapacity() != null && r.getCapacity() > 0)
        .filter(r -> r.getSlotStartTime() != null && r.getSlotDuration() != null)
        .toList();
  }
}

