package com.peti.backend.service.rrule;

import com.peti.backend.model.domain.CaretakerRRule;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.extern.slf4j.Slf4j;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;

@Slf4j
public class RRuleUtils {

  /**
   * Checks if the given rrule produces an occurrence on the specified date. Uses the rrule's createdAt as DTSTART
   * anchor to correctly resolve the recurrence phase (important for intervals > 1, e.g. "every 2 weeks").
   */
  public static boolean isActiveOnDate(CaretakerRRule rrule, LocalDate date) {
    String rruleStr = rrule.getRrule();
    if (rruleStr == null || rruleStr.isBlank()) {
      return false;
    }
    try {
      RecurrenceRule recurrenceRule = new RecurrenceRule(rruleStr);
      LocalDateTime dtStart = rrule.getCreatedAt().toLocalDate().atTime(rrule.getSlotStartTime());
      DateTime startDateTimePoint = toRfc5545(dtStart);

      RecurrenceRuleIterator iterator = recurrenceRule.iterator(startDateTimePoint);
      // Fast-forward: skip occurrences before the target date
      iterator.fastForward(toRfc5545(date.atStartOfDay()));

      // After fast-forward, check if next occurrence falls on the target date
      if (iterator.hasNext()) {
        LocalDate occurrenceDate = toLocalDate(iterator.nextDateTime());
        return occurrenceDate.equals(date);
      }
      return false;
    } catch (Exception e) {
      log.warn("Failed to parse rrule '{}', assuming not active on {}: {}",
          rruleStr, date, e.getMessage());
      return false;
    }
  }

  private static DateTime toRfc5545(LocalDateTime dateTime) {
    long epochMilli = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    return new DateTime(epochMilli);
  }

  private static LocalDate toLocalDate(DateTime dateTime) {
    return LocalDate.ofInstant(Instant.ofEpochMilli(dateTime.getTimestamp()), ZoneId.systemDefault());
  }
}
