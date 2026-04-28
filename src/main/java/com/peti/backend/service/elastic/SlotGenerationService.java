package com.peti.backend.service.elastic;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

  private final ElasticSlotAssembler assembler;

  /**
   * Generate all capacity-layered slot documents for a single caretaker on a single day.
   *
   * @param date      date to generate slots for
   * @param rrules    availability rules active on that day (must share the same caretaker)
   * @param bookings  existing bookings that reduce available capacity
   * @param caretaker the caretaker domain entity (must have userReference loaded)
   * @return list of generated slot documents ready for indexing
   */
  public List<ElasticSlotDocument> generateSlotsForDay(
      LocalDate date,
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      Caretaker caretaker
  ) {
    List<CaretakerRRule> validRules = filterValidRules(rrules);
    if (validRules.isEmpty()) {
      return List.of();
    }

    List<TimeSegmentWithPricing> segments = CapacityTimelineBuilder.buildSegments(
        validRules, bookings, caretaker.getCaretakerPreference()
    );

    int maxCapacity = segments.stream().mapToInt(TimeSegmentWithPricing::capacity).max().orElse(0);
    if (maxCapacity == 0) {
      return List.of();
    }

    Map<Integer, List<TimeRange>> capacityRanges = SlotRangeResolver.resolveRangesByCapacity(segments);

    return assembler.assemble(date, capacityRanges, segments, caretaker);
  }

  /** Filter out disabled rules and rules with invalid time ranges or zero capacity. */
  private List<CaretakerRRule> filterValidRules(List<CaretakerRRule> rrules) {
    if (rrules == null || rrules.isEmpty()) {
      return List.of();
    }
    return rrules.stream()
        .filter(r -> Boolean.TRUE.equals(r.getIsEnabled()))
        .filter(r -> r.getCapacity() != null && r.getCapacity() > 0)
        .filter(r -> r.getDtstart() != null && r.getDtend() != null)
        .filter(r -> r.getDtstart().isBefore(r.getDtend()))
        .toList();
  }
}

