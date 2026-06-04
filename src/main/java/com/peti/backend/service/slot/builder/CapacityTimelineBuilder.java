package com.peti.backend.service.slot.builder;

import static com.peti.backend.model.elastic.model.Capacity.fromNegativeBooking;
import static com.peti.backend.model.elastic.model.Capacity.fromNegativeRRule;
import static com.peti.backend.model.elastic.model.Capacity.fromPositiveBooking;
import static com.peti.backend.model.elastic.model.Capacity.fromPositiveRRule;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.Capacity;
import com.peti.backend.model.elastic.model.TimeSegment;
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
 *   <li>Convert boundary map into consecutive {@link TimeSegment} entries</li>
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
  public static List<TimeSegment> buildSegments(
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      LocalDate date) {

    List<CaretakerRRule> activeRules = filterActiveOnDate(rrules, date);
    if (activeRules.isEmpty()) {
      return List.of();
    }

    TreeMap<LocalDateTime, Capacity> timeline = buildTimeline(activeRules, bookings, date);
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
  private static TreeMap<LocalDateTime, Capacity> buildTimeline(List<CaretakerRRule> activeRules,
      List<BookingInput> bookings, LocalDate date) {
    TreeMap<LocalDateTime, Capacity> deltas = new TreeMap<>();

    for (CaretakerRRule rrule : activeRules) {
      LocalDateTime start = date.atTime(rrule.getSlotStartTime());
      LocalDateTime end = start.plus(rrule.getSlotDuration());
      deltas.merge(start, fromPositiveRRule(rrule), Capacity::sum);
      deltas.merge(end, fromNegativeRRule(rrule), Capacity::sum);
    }

    for (BookingInput booking : bookings) {
      deltas.merge(booking.timeFrom(), fromNegativeBooking(booking), Capacity::sum);
      deltas.merge(booking.timeTo(), fromPositiveBooking(booking), Capacity::sum);
    }

    TreeMap<LocalDateTime, Capacity> timeline = new TreeMap<>();
    Capacity runningCapacity = new Capacity(0, 0);

    for (var entry : deltas.entrySet()) {
      runningCapacity = runningCapacity.sum(entry.getValue());
      timeline.put(entry.getKey(), runningCapacity.clampToZero());
    }

    return timeline;
  }


  /**
   * Convert the boundary map into consecutive time segments. Only segments with positive capacity are included.
   */
  private static List<TimeSegment> convertToSegments(TreeMap<LocalDateTime, Capacity> timeline) {
    List<TimeSegment> segments = new ArrayList<>();
    List<LocalDateTime> boundaries = new ArrayList<>(timeline.keySet());

    for (int i = 0; i < boundaries.size() - 1; i++) {
      LocalDateTime segStart = boundaries.get(i);
      LocalDateTime segEnd = boundaries.get(i + 1);
      Capacity capacity = timeline.get(segStart);

      if (segStart.isBefore(segEnd) && capacity.isPositive()) {
        segments.add(new TimeSegment(segStart, segEnd, capacity.getCapacity()));
      }
    }
    return segments;
  }
}

