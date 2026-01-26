package com.peti.backend.service;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    LocalDate endDate = today.plusDays(daysAhead);
    LocalDateTime now = LocalDateTime.now();

    log.info("Generating slots from {} to {} ({} days ahead)", today, endDate, daysAhead);

    // Fetch all active RRules
    List<CaretakerRRule> activeRRules = rruleRepository.findAllActive(now);
    log.info("Found {} active RRules to process", activeRRules.size());

    int totalSlotsCreated = 0;
    int rruleProcessed = 0;
    int errors = 0;

    // Process RRules in batches
    for (int i = 0; i < activeRRules.size(); i += batchSize) {
      int endIndex = Math.min(i + batchSize, activeRRules.size());
      List<CaretakerRRule> batch = activeRRules.subList(i, endIndex);

      log.debug("Processing batch {}-{} of {} RRules", i + 1, endIndex, activeRRules.size());

      for (CaretakerRRule rrule : batch) {
        try {
          int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, today, endDate);
          totalSlotsCreated += slotsCreated;
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
   * Result statistics for slot generation.
   */
  public record SlotGenerationResult(int rruleProcessed, int slotsCreated, int errors) {
  }
}

