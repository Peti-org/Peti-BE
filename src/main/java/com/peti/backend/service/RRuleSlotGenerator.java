package com.peti.backend.service;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.repository.SlotRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.recur.InvalidRecurrenceRuleException;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import org.dmfs.rfc5545.recur.RecurrenceRuleIterator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for generating slots based on RRule configurations.
 * Uses RFC 5545 recurrence rules to calculate slot occurrences.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RRuleSlotGenerator {

  private final SlotRepository slotRepository;
  private final EntityManager entityManager;

  // Mock values for now as per plan
  private static final BigDecimal DEFAULT_PRICE = new BigDecimal("100.00");
  private static final String DEFAULT_CURRENCY = "UAH";
  private static final int DEFAULT_CAPACITY = 5;

  /**
   * Generates slots for a single RRule within the specified date range.
   *
   * @param rrule The recurrence rule configuration
   * @param startDate The start date for slot generation
   * @param endDate The end date for slot generation
   * @return Number of slots created
   */
  @Transactional
  public int generateSlotsForRRule(CaretakerRRule rrule, LocalDate startDate, LocalDate endDate) {
    try {
      log.debug("Generating slots for RRule {} from {} to {}", rrule.getRruleId(), startDate, endDate);

      // Parse the RRule string
      RecurrenceRule recurrenceRule = new RecurrenceRule(rrule.getRrule());

      // Get start and end times from dtstart
      LocalTime dailyStartTime = rrule.getDtstart().toLocalTime();
      LocalTime dailyEndTime = rrule.getDtend() != null ? rrule.getDtend().toLocalTime() : dailyStartTime.plusHours(8);

      // Convert LocalDate to DateTime for the iterator
      DateTime start = new DateTime(
          java.util.Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime()
      );
      DateTime end = new DateTime(
          java.util.Date.from(endDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime()
      );

      // Get occurrences in the date range
      RecurrenceRuleIterator iterator = recurrenceRule.iterator(
          new DateTime(rrule.getDtstart().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
      );

      List<Slot> slotsToCreate = new ArrayList<>();
      int slotsCreated = 0;

      // Iterate through recurrence occurrences
      while (iterator.hasNext()) {
        DateTime occurrence = iterator.nextDateTime();
        LocalDate occurrenceDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(occurrence.getTimestamp()),
            ZoneId.systemDefault()
        );

        // Check if occurrence is within our target range
        if (occurrenceDate.isBefore(startDate)) {
          continue;
        }
        if (occurrenceDate.isAfter(endDate)) {
          break;
        }

        // Check if dtend restriction applies
        if (rrule.getDtend() != null && occurrenceDate.isAfter(rrule.getDtend().toLocalDate())) {
          break;
        }

        // Create slot for this occurrence
        Slot slot = createSlot(rrule, occurrenceDate, dailyStartTime, dailyEndTime);

        // Check if slot already exists
        if (!slotRepository.existsByCaretakerIdAndDateAndTime(
            rrule.getCaretaker().getCaretakerId(),
            Date.valueOf(occurrenceDate),
            Time.valueOf(dailyStartTime),
            Time.valueOf(dailyEndTime))) {
          slotsToCreate.add(slot);
          slotsCreated++;
        }

        // Batch save every 100 slots to avoid memory issues
        if (slotsToCreate.size() >= 100) {
          slotRepository.saveAll(slotsToCreate);
          slotsToCreate.clear();
          log.debug("Batch saved 100 slots for RRule {}", rrule.getRruleId());
        }
      }

      // Save remaining slots
      if (!slotsToCreate.isEmpty()) {
        slotRepository.saveAll(slotsToCreate);
        log.debug("Saved final batch of {} slots for RRule {}", slotsToCreate.size(), rrule.getRruleId());
      }

      log.info("Generated {} slots for RRule {}", slotsCreated, rrule.getRruleId());
      return slotsCreated;

    } catch (InvalidRecurrenceRuleException e) {
      log.error("Invalid RRule format for RRule {}: {}", rrule.getRruleId(), e.getMessage(), e);
      return 0;
    } catch (Exception e) {
      log.error("Error generating slots for RRule {}: {}", rrule.getRruleId(), e.getMessage(), e);
      return 0;
    }
  }

  /**
   * Creates a Slot entity from RRule configuration.
   */
  private Slot createSlot(CaretakerRRule rrule, LocalDate date, LocalTime timeFrom, LocalTime timeTo) {
    Slot slot = new Slot();
    slot.setCaretaker(rrule.getCaretaker());
    slot.setDate(Date.valueOf(date));
    slot.setTimeFrom(Time.valueOf(timeFrom));
    slot.setTimeTo(Time.valueOf(timeTo));
    slot.setType(rrule.getSlotType());
    slot.setPrice(DEFAULT_PRICE);
    slot.setCurrency(DEFAULT_CURRENCY);
    slot.setCreationTime(LocalDateTime.now());
    slot.setAdditionalData("{}");
    slot.setAvailable(true);
    slot.setCapacity(DEFAULT_CAPACITY);
    slot.setOccupiedCapacity(0);
    return slot;
  }
}

