package com.peti.backend.service.slot;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.SlotRepository;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled component that automatically generates caretaker slots based on RRule configurations.
 * Runs daily to ensure slots are available for the next 14 days.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationScheduler {

  private final CaretakerRRuleRepository rruleRepository;
  private final RRuleSlotGenerator slotGenerator;
  private final SlotRepository slotRepository;

  @Value("${app.slot-generation.days-ahead:14}")
  private int daysAhead;

  @Value("${app.slot-generation.batch-size:50}")
  private int batchSize;

  /**
   * Scheduled job that runs daily to generate slots for the next 14 days.
   * Cron expression is configured via application.properties.
   */
  @Scheduled(cron = "${app.slot-generation.cron:0 0 2 * * *}")
  public void generateDailySlots() {
    log.info("Starting scheduled slot generation job");
    long startTime = System.currentTimeMillis();

    try {
      SlotGenerationResult result = generateSlots();

      long duration = System.currentTimeMillis() - startTime;
      log.info("Completed slot generation: {} RRules processed, {} slots created, {} errors in {}ms",
          result.rruleProcessed, result.slotsCreated, result.errors, duration);

    } catch (Exception e) {
      log.error("Fatal error in scheduled slot generation: {}", e.getMessage(), e);
    }
  }

  /**
   * Generates slots for all active RRules.
   * This method can be called manually or by the scheduler.
   *
   * @return Generation result statistics
   */
  public SlotGenerationResult generateSlots() {
    LocalDate today = LocalDate.now();
    LocalDate targetEndDate = today.plusDays(daysAhead);
    LocalDateTime now = LocalDateTime.now();

    log.info("Generating slots up to {} ({} days ahead)", targetEndDate, daysAhead);

    // Fetch RRules that need generation (generatedTo is null or before targetEndDate)
    List<CaretakerRRule> rrulesToProcess = rruleRepository.findAllNeedingGeneration(targetEndDate, now);
    log.info("Found {} RRules needing slot generation", rrulesToProcess.size());

    int totalSlotsCreated = 0;
    int rruleProcessed = 0;
    int errors = 0;

    // Process RRules in batches
    for (int i = 0; i < rrulesToProcess.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, rrulesToProcess.size());
      List<CaretakerRRule> batch = rrulesToProcess.subList(i, endIndex);

      log.debug("Processing batch {}-{} of {} RRules", i + 1, endIndex, rrulesToProcess.size());

      for (CaretakerRRule rrule : batch) {
        try {
          // Determine start date: either today or day after last generated date
          LocalDate startDate = rrule.getGeneratedTo() != null
              ? rrule.getGeneratedTo().plusDays(1)
              : today;

          // Only generate if there's a date range to cover
          if (!startDate.isAfter(targetEndDate)) {
            int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, targetEndDate);
            totalSlotsCreated += slotsCreated;
            log.debug("Generated {} slots for RRule {} from {} to {}",
                slotsCreated, rrule.getRruleId(), startDate, targetEndDate);
          }
          rruleProcessed++;
        } catch (Exception e) {
          log.error("Error processing RRule {}: {}", rrule.getRruleId(), e.getMessage(), e);
          errors++;
        }
      }

      log.debug("Completed batch processing: {} RRules processed so far", rruleProcessed);
    }

    return new SlotGenerationResult(rruleProcessed, totalSlotsCreated, errors);
  }

  /**
   * Generates slots for a single RRule. Used when RRule is created or updated.
   *
   * @param rruleId The RRule ID
   * @param startDate Start date for generation
   * @param endDate End date for generation
   * @return Number of slots created
   */
  @Transactional
  public int generateSlotsForSingleRRule(UUID rruleId, LocalDate startDate, LocalDate endDate) {
    return rruleRepository.findById(rruleId)
        .map(rrule -> {
          try {
            int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);
            log.info("Generated {} slots for single RRule {} from {} to {}",
                slotsCreated, rruleId, startDate, endDate);
            return slotsCreated;
          } catch (Exception e) {
            log.error("Error generating slots for RRule {}: {}", rruleId, e.getMessage(), e);
            return 0;
          }
        })
        .orElseGet(() -> {
          log.warn("RRule {} not found for slot generation", rruleId);
          return 0;
        });
  }

  /**
   * Deletes future unoccupied slots generated by a specific RRule.
   * Used when RRule is updated or deleted.
   *
   * @param rruleId The RRule ID
   * @param fromDate Delete slots from this date onwards
   * @return Number of slots deleted
   */
  @Transactional
  public int deleteSlotsForRRule(UUID rruleId, LocalDate fromDate) {
    try {
      int deletedCount = slotRepository.deleteByRRuleIdAndDateAfterAndUnoccupied(
          rruleId,
          Date.valueOf(fromDate)
      );
      log.info("Deleted {} unoccupied slots for RRule {} from date {}",
          deletedCount, rruleId, fromDate);
      return deletedCount;
    } catch (Exception e) {
      log.error("Error deleting slots for RRule {}: {}", rruleId, e.getMessage(), e);
      return 0;
    }
  }

  /**
   * Result statistics for slot generation.
   */
  public record SlotGenerationResult(int rruleProcessed, int slotsCreated, int errors) {
  }
}

