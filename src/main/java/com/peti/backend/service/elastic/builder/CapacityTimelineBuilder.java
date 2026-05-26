package com.peti.backend.service.elastic.builder;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.CapacityWithPricing;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import com.peti.backend.service.slot.RRuleUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless utility that builds a capacity timeline for a single day by merging
 * multiple RRules and subtracting bookings, then converts it to discrete time segments.
 *
 * <p>Algorithm:
 * <ol>
 *   <li>Filter RRules whose recurrence pattern includes the given date</li>
 *   <li>Collect all time boundaries from rrule start/end and booking start/end</li>
 *   <li>At each boundary, compute net capacity = sum of active rrules − sum of active bookings</li>
 *   <li>Convert boundary map into consecutive {@link TimeSegmentWithPricing} entries</li>
 * </ol>
 */
@Slf4j
public final class CapacityTimelineBuilder {

  private CapacityTimelineBuilder() {
  }

  /**
   * Build time segments for a concrete date from the given RRules and bookings.
   *
   * @param rrules        availability rules (may include rules not active on this date)
   * @param bookings      existing bookings that reduce available capacity
   * @param date          the concrete date to generate segments for
   * @return ordered list of time segments with positive capacity
   */
  public static List<TimeSegmentWithPricing> buildSegments(
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      LocalDate date) {

    List<CaretakerRRule> activeRules = filterActiveOnDate(rrules, date);
    if (activeRules.isEmpty()) {
      return List.of();
    }

    TreeMap<LocalDateTime, CapacityWithPricing> timeline = buildTimeline(activeRules, bookings, date);
    return convertToSegments(timeline);
  }

  /**
   * Returns only RRules whose recurrence pattern includes the given date.
   * If the rrule string is missing or unparseable, the rule is included by default.
   */
  static List<CaretakerRRule> filterActiveOnDate(List<CaretakerRRule> rrules, LocalDate date) {
    return rrules.stream()
        .filter(r -> RRuleUtils.isActiveOnDate(r, date))
        .toList();
  }

  /**
   * Build a sorted map of boundary-time → capacity for all active rules and bookings.
   */
  private static TreeMap<LocalDateTime, CapacityWithPricing> buildTimeline(
      List<CaretakerRRule> activeRules,
      List<BookingInput> bookings,
      LocalDate date) {

    TreeMap<LocalDateTime, CapacityWithPricing> timeline = new TreeMap<>();

    for (CaretakerRRule rrule : activeRules) {
      LocalDateTime rruleStart = date.atTime(rrule.getSlotStartTime());
      LocalDateTime rruleEnd = rruleStart.plus(rrule.getSlotDuration());
      timeline.put(rruleStart, null);
      timeline.put(rruleEnd, null);
    }
    for (BookingInput booking : bookings) {
      timeline.put(booking.timeFrom(), null);
      timeline.put(booking.timeTo(), null);
    }

    for (LocalDateTime boundary : new ArrayList<>(timeline.keySet())) {
      int capacity = computeNetCapacity(boundary, activeRules, bookings, date);
      timeline.put(boundary, new CapacityWithPricing(capacity));
    }

    return timeline;
  }

  /**
   * Compute net capacity at a given time point:
   * sum of rrule capacities covering that time minus sum of booking capacities.
   */
  private static int computeNetCapacity(
      LocalDateTime time,
      List<CaretakerRRule> activeRules,
      List<BookingInput> bookings,
      LocalDate date) {

    int capacity = 0;
    for (CaretakerRRule rrule : activeRules) {
      LocalDateTime from = date.atTime(rrule.getSlotStartTime());
      LocalDateTime to = from.plus(rrule.getSlotDuration());
      if (!time.isBefore(from) && time.isBefore(to)) {
        capacity += rrule.getCapacity();
      }
    }
    for (BookingInput booking : bookings) {
      if (!time.isBefore(booking.timeFrom()) && time.isBefore(booking.timeTo())) {
        capacity -= booking.bookedCapacity();
      }
    }
    return Math.max(0, capacity);
  }

  /**
   * Convert the boundary map into consecutive time segments.
   * Only segments with positive capacity are included.
   */
  private static List<TimeSegmentWithPricing> convertToSegments(
      TreeMap<LocalDateTime, CapacityWithPricing> timeline) {

    List<TimeSegmentWithPricing> segments = new ArrayList<>();
    List<LocalDateTime> boundaries = new ArrayList<>(timeline.keySet());

    for (int i = 0; i < boundaries.size() - 1; i++) {
      LocalDateTime segStart = boundaries.get(i);
      LocalDateTime segEnd = boundaries.get(i + 1);
      CapacityWithPricing info = timeline.get(segStart);

      if (segStart.isBefore(segEnd) && info.capacity() > 0) {
        segments.add(new TimeSegmentWithPricing(
            segStart, segEnd, info.capacity()));
      }
    }
    return segments;
  }
}

