package com.peti.backend.service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import com.peti.backend.model.internal.TimeSlotPair;
import org.springframework.stereotype.Component;

// ==================== SlotDivider Component ====================
@Component
public class SlotDivider {

  /**
   * Divides a time range into slots of specified duration, aligned to interval boundaries
   *
   * @param startTime Original start time
   * @param endTime Original end time
   * @param intervalMinutes Duration of each slot in minutes (e.g., 15, 30, 40)
   * @return List of time slot pairs (start, end)
   */
  public List<TimeSlotPair> divideTimeRange(LocalTime startTime, LocalTime endTime, int intervalMinutes) {
    List<TimeSlotPair> timeSlots = new ArrayList<>();

    // Validate interval
    if (intervalMinutes <= 0 || intervalMinutes > 1440) {
      throw new IllegalArgumentException("Interval must be between 1 minute and 1 day (1440 minutes)");
    }

    // Normalize start time to next interval boundary
    LocalTime normalizedStart = normalizeToNextBoundary(startTime, intervalMinutes);

    // Normalize end time to previous interval boundary
    LocalTime normalizedEnd = normalizeToPreviousBoundary(endTime, intervalMinutes);

    // If normalized times don't allow for any slots, return empty list
    if (!normalizedStart.isBefore(normalizedEnd)) {
      return timeSlots;
    }

    // Generate time slots
    LocalTime current = normalizedStart;
    while (current.isBefore(normalizedEnd)) {
      LocalTime slotEnd = current.plusMinutes(intervalMinutes);

      // Ensure we don't exceed the requested end time
      if (slotEnd.isAfter(normalizedEnd)) {
        break;
      }

      timeSlots.add(new TimeSlotPair(current, slotEnd));
      current = slotEnd;
    }

    return timeSlots;
  }

  /**
   * Normalizes time to the next interval boundary
   * Examples with 30-minute intervals:
   * - 12:07 -> 12:30
   * - 12:00 -> 12:00
   * - 12:31 -> 13:00
   *
   * Examples with 15-minute intervals:
   * - 12:07 -> 12:15
   * - 12:15 -> 12:15
   * - 12:46 -> 13:00
   */
  private LocalTime normalizeToNextBoundary(LocalTime time, int intervalMinutes) {
    int totalMinutes = time.getHour() * 60 + time.getMinute();
    int remainder = totalMinutes % intervalMinutes;

    if (remainder == 0) {
      return time.withSecond(0).withNano(0);
    }

    int minutesToAdd = intervalMinutes - remainder;
    return time.plusMinutes(minutesToAdd).withSecond(0).withNano(0);
  }

  /**
   * Normalizes time to the previous interval boundary
   * Examples with 30-minute intervals:
   * - 12:45 -> 12:30
   * - 13:00 -> 13:00
   * - 12:29 -> 12:00
   *
   * Examples with 15-minute intervals:
   * - 12:45 -> 12:45
   * - 12:46 -> 12:45
   * - 12:14 -> 12:00
   */
  private LocalTime normalizeToPreviousBoundary(LocalTime time, int intervalMinutes) {
    int totalMinutes = time.getHour() * 60 + time.getMinute();
    int remainder = totalMinutes % intervalMinutes;

    if (remainder == 0) {
      return time.withSecond(0).withNano(0);
    }

    return time.minusMinutes(remainder).withSecond(0).withNano(0);
  }
}
