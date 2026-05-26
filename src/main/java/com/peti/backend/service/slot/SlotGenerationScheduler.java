package com.peti.backend.service.slot;

import com.peti.backend.dto.slot.SlotGenerationMode;
import com.peti.backend.dto.slot.SlotGenerationRequest;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.repository.CaretakerRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component that generates caretaker slots. Supports two modes: DEFAULT (incremental) and FORCE (full
 * regeneration).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SlotGenerationScheduler {

  private final SlotsRebuildTrigger slotsRebuildTrigger;
  private final CaretakerRepository caretakerRepository;

  @Value("${elasticsearch.data-generation.days-ahead:60}")
  private int daysAhead;

  /**
   * Scheduled job — runs in DEFAULT (incremental) mode daily.
   */
  @Scheduled(cron = "${app.slot-generation.cron:0 0 2 * * *}")
  public void generateDailySlots() {
    log.info("[SLOT-GEN] Starting scheduled slot generation in DEFAULT mode");
    SlotGenerationRequest request = new SlotGenerationRequest(SlotGenerationMode.DEFAULT, null, null, null);
    SlotGenerationResult result = generateSlots(request);
    log.info("[SLOT-GEN] Completed scheduled DEFAULT generation: {}", result);
  }

  /**
   * Entry point for slot generation triggered by admin or scheduler.
   */
  public SlotGenerationResult generateSlots(SlotGenerationRequest request) {
    SlotGenerationMode mode = request.modeOrDefault();
    log.info("[SLOT-GEN] Running slot generation | mode={} | caretakerId={} | dateFrom={} | dateTo={}",
        mode, request.caretakerId(), request.dateFrom(), request.dateTo());

    long startTime = System.currentTimeMillis();
    List<Caretaker> caretakers = resolveCaretakers(request.caretakerId());
    int processed = 0;
    int errors = 0;

    for (Caretaker caretaker : caretakers) {
      try {
        generateSlotsForCaretaker(caretaker, mode, request.dateFrom(), request.dateTo());
        processed++;
      } catch (Exception e) {
        errors++;
        log.error("[SLOT-GEN] Error processing caretaker {}: {}", caretaker.getCaretakerId(), e.getMessage(), e);
      }
    }

    long duration = System.currentTimeMillis() - startTime;
    SlotGenerationResult result = new SlotGenerationResult(processed, duration, errors);
    log.info("[SLOT-GEN] Finished | mode={} | result={}", mode, result);
    return result;
  }

  private List<Caretaker> resolveCaretakers(UUID caretakerId) {
    if (caretakerId == null) {
      return caretakerRepository.findAll();
    }
    return caretakerRepository.findById(caretakerId)
        .map(List::of)
        .orElseGet(() -> {
          log.warn("[SLOT-GEN] Caretaker not found: {}", caretakerId);
          return List.of();
        });
  }

  private void generateSlotsForCaretaker(Caretaker caretaker, SlotGenerationMode mode,
      LocalDate requestFrom, LocalDate requestTo) {
    LocalDate today = LocalDate.now();
    LocalDate endDate = requestTo != null ? requestTo : today.plusDays(daysAhead);
    LocalDate startDate;

    if (mode == SlotGenerationMode.FORCE) {
      startDate = requestFrom != null ? requestFrom : today;
    } else {
      // DEFAULT/incremental: start from generatedTo (or today if null)
      LocalDate generatedTo = caretaker.getGeneratedTo();
      startDate = generatedTo != null ? generatedTo.plusDays(1) : today;
      if (requestFrom != null && requestFrom.isAfter(startDate)) {
        startDate = requestFrom;
      }
    }

    if (startDate.isAfter(endDate)) {
      log.debug("[SLOT-GEN] Caretaker {} already generated up to {}, nothing to do",
          caretaker.getCaretakerId(), caretaker.getGeneratedTo());
      return;
    }

    log.info("[SLOT-GEN] Processing caretaker {} | mode={} | range=[{} -> {}]",
        caretaker.getCaretakerId(), mode, startDate, endDate);

    slotsRebuildTrigger.rebuild(caretaker.getCaretakerId(), startDate, endDate);

    // Update generatedTo watermark
    caretaker.setGeneratedTo(endDate);
    caretakerRepository.save(caretaker);
  }

  public record SlotGenerationResult(int caretakersProcessed, long durationMs, int errors) {

  }
}
