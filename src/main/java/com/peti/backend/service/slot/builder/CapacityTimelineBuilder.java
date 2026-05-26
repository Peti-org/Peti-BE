package com.peti.backend.service.slot.builder;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.CapacityWithPricing;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import com.peti.backend.service.rrule.RRuleUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;

/**
 * Stateless utility that builds a capacity timeline for a single day by merging multiple RRules and subtracting
 * bookings, then converts it to discrete time segments.
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
   * @param rrules   availability rules (may include rules not active on this date)
   * @param bookings existing bookings that reduce available capacity
   * @param date     the concrete date to generate segments for
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
   * Returns only RRules whose recurrence pattern includes the given date. If the rrule string is missing or
   * unparseable, the rule is included by default.
   */
  static List<CaretakerRRule> filterActiveOnDate(List<CaretakerRRule> rrules, LocalDate date) {
    return rrules.stream()
        .filter(r -> RRuleUtils.isActiveOnDate(r, date))
        .toList();
  }

  /**
   * Sweep-line algorithm: collect capacity deltas at each boundary, then sweep left-to-right accumulating a running
   * total. O((R+K) log(R+K)) complexity.
   */
  private static TreeMap<LocalDateTime, CapacityWithPricing> buildTimeline(List<CaretakerRRule> activeRules,
      List<BookingInput> bookings, LocalDate date) {
    TreeMap<LocalDateTime, Integer> deltas = new TreeMap<>();

    for (CaretakerRRule rrule : activeRules) {
      LocalDateTime start = date.atTime(rrule.getSlotStartTime());
      LocalDateTime end = start.plus(rrule.getSlotDuration());
      deltas.merge(start, rrule.getCapacity(), Integer::sum);
      deltas.merge(end, -rrule.getCapacity(), Integer::sum);
    }

    for (BookingInput booking : bookings) {
      deltas.merge(booking.timeFrom(), -booking.bookedCapacity(), Integer::sum);
      deltas.merge(booking.timeTo(), booking.bookedCapacity(), Integer::sum);
    }

    TreeMap<LocalDateTime, CapacityWithPricing> timeline = new TreeMap<>();
    int runningCapacity = 0;

    for (var entry : deltas.entrySet()) {
      runningCapacity += entry.getValue();
      timeline.put(entry.getKey(), new CapacityWithPricing(Math.max(0, runningCapacity)));
    }

    return timeline;
  }


  /**
   * Convert the boundary map into consecutive time segments. Only segments with positive capacity are included.
   */
  private static List<TimeSegmentWithPricing> convertToSegments(TreeMap<LocalDateTime, CapacityWithPricing> timeline) {
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

