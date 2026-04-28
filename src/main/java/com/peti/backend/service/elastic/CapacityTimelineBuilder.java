package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.CapacityWithPricing;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stateless utility that builds a capacity timeline by merging multiple RRules
 * and subtracting bookings, then converts it to discrete time segments.
 *
 * <p>When RRules overlap, capacities are SUMMED. The ServiceConfig of the
 * highest-priority active RRule wins at each boundary point.
 */
public final class CapacityTimelineBuilder {

  private CapacityTimelineBuilder() {}

  /**
   * Build a list of time segments from the given RRules and bookings.
   * The ServiceConfig per RRule is resolved from the caretaker's preferences by slot type.
   */
  public static List<TimeSegmentWithPricing> buildSegments(
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      CaretakerPreferences preferences
  ) {
    Map<LocalTime, CapacityWithPricing> timeline = buildTimeline(rrules, bookings, preferences);
    return convertToSegments(timeline);
  }

  private static Map<LocalTime, CapacityWithPricing> buildTimeline(
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      CaretakerPreferences preferences
  ) {
    TreeMap<LocalTime, CapacityWithPricing> timeline = new TreeMap<>();

    for (CaretakerRRule rrule : rrules) {
      timeline.putIfAbsent(rrule.getDtstart().toLocalTime(), new CapacityWithPricing(0, null));
      timeline.putIfAbsent(rrule.getDtend().toLocalTime(), new CapacityWithPricing(0, null));
    }
    for (BookingInput booking : bookings) {
      timeline.putIfAbsent(booking.timeFrom(), new CapacityWithPricing(0, null));
      timeline.putIfAbsent(booking.timeTo(), new CapacityWithPricing(0, null));
    }

    for (LocalTime time : new ArrayList<>(timeline.keySet())) {
      timeline.put(time, computeCapacityAt(time, rrules, bookings, preferences));
    }

    return timeline;
  }

  private static CapacityWithPricing computeCapacityAt(
      LocalTime time,
      List<CaretakerRRule> rrules,
      List<BookingInput> bookings,
      CaretakerPreferences preferences
  ) {
    int totalCapacity = 0;
    ServiceConfig selectedConfig = null;
    int highestPriority = Integer.MIN_VALUE;

    for (CaretakerRRule rrule : rrules) {
      LocalTime from = rrule.getDtstart().toLocalTime();
      LocalTime to = rrule.getDtend().toLocalTime();
      if (!time.isBefore(from) && time.isBefore(to)) {
        totalCapacity += rrule.getCapacity();
        if (rrule.getPriority() > highestPriority
            || (rrule.getPriority() == highestPriority && selectedConfig == null)) {
          highestPriority = rrule.getPriority();
          selectedConfig = resolveServiceConfig(rrule.getSlotType(), preferences);
        }
      }
    }
    for (BookingInput booking : bookings) {
      if (!time.isBefore(booking.timeFrom()) && time.isBefore(booking.timeTo())) {
        totalCapacity -= booking.bookedCapacity();
      }
    }

    return new CapacityWithPricing(Math.max(0, totalCapacity), selectedConfig);
  }

  /** Resolve the ServiceConfig matching the rrule's slotType name (case-insensitive). */
  private static ServiceConfig resolveServiceConfig(String slotType, CaretakerPreferences preferences) {
    if (slotType == null || preferences == null || preferences.services() == null) {
      return null;
    }
    return preferences.services().stream()
        .filter(s -> s.type() != null && s.type().name().equalsIgnoreCase(slotType))
        .findFirst()
        .orElse(null);
  }

  private static List<TimeSegmentWithPricing> convertToSegments(Map<LocalTime, CapacityWithPricing> timeline) {
    List<TimeSegmentWithPricing> segments = new ArrayList<>();
    List<LocalTime> times = new ArrayList<>(timeline.keySet());
    times.sort(Comparator.naturalOrder());

    for (int i = 0; i < times.size() - 1; i++) {
      LocalTime segStart = times.get(i);
      LocalTime segEnd = times.get(i + 1);
      CapacityWithPricing info = timeline.get(segStart);

      if (segStart.isBefore(segEnd) && info.capacity() > 0 && info.serviceConfig() != null) {
        segments.add(new TimeSegmentWithPricing(segStart, segEnd, info.capacity(), info.serviceConfig()));
      }
    }

    return segments;
  }
}


