package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.service.elastic.model.BookingInput;
import com.peti.backend.service.elastic.model.CapacityWithPricing;
import java.time.LocalTime;
import java.util.List;

/**
 * Computes the effective capacity and active {@link ServiceConfig} at a given
 * point in time, considering active RRules and bookings.
 */
final class CapacityCalculator {

  private CapacityCalculator() {}

  /**
   * Compute the net capacity (rrule sum minus bookings) at a specific time,
   * and resolve the highest-priority {@link ServiceConfig}.
   */
  static CapacityWithPricing computeAt(
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
  private static ServiceConfig resolveServiceConfig(String slotType, CaretakerPreferences prefs) {
    if (slotType == null || prefs == null || prefs.services() == null) {
      return null;
    }
    return prefs.services().stream()
        .filter(s -> s.type() != null && s.type().name().equalsIgnoreCase(slotType))
        .findFirst()
        .orElse(null);
  }
}

