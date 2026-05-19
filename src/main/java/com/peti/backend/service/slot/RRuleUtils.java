package com.peti.backend.service.slot;

import com.peti.backend.model.domain.CaretakerRRule;
import lombok.extern.slf4j.Slf4j;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

@Slf4j
public class RRuleUtils {

  public static boolean isActiveOnDate(CaretakerRRule rrule, LocalDate date) {
    String rruleStr = rrule.getRrule();
    if (rruleStr == null || rruleStr.isBlank()) {
      return true;
    }
    try {
      RecurrenceRule recurrenceRule = new RecurrenceRule(rruleStr);
      LocalTime startTime = rrule.getSlotStartTime();
      LocalDateTime dtStart = date.minusYears(1).atTime(startTime);
      DateTime rfc5545Start = new DateTime(
          dtStart.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
      RecurrenceRuleIterator iterator = recurrenceRule.iterator(rfc5545Start);

      while (iterator.hasNext()) {
        DateTime occurrence = iterator.nextDateTime();
        LocalDate occurrenceDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(occurrence.getTimestamp()),
            ZoneId.systemDefault());

        if (occurrenceDate.equals(date)) {
          return true;
        }
        if (occurrenceDate.isAfter(date)) {
          return false;
        }
      }
      return false;
    } catch (Exception e) {
      log.warn("Failed to parse rrule '{}', assuming active on {}: {}",
          rruleStr, date, e.getMessage());
      return true;
    }
  }

}
