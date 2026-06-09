package com.peti.backend.service.slot;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.repository.EventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.peti.backend.service.slot.builder.SlotGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Triggers a rebuild of Elastic slot documents for a caretaker over a date range.
 *
 * <p>Used when an event is created or deleted: the affected days have their Elastic
 * slot documents replaced based on current RRules and remaining (non-deleted) events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlotsRebuildTrigger {

  private final CaretakerRRuleRepository rruleRepository;
  private final CaretakerRepository caretakerRepository;
  private final EventRepository eventRepository;
  private final SlotGenerationService slotGenerationService;
  private final SlotCrudService slotCrudService;

  @Value("${elasticsearch.data-generation.days-ahead:60}")
  private int daysAhead;

  /**
   * Triggers a rebuild of Elastic slot documents for given caretaker for the next {@code daysAhead} days starting from
   * today.
   */
  @Async
  public void rebuildAsync(UUID caretakerId) {
    LocalDate current = LocalDate.now();
    LocalDate end = current.plusDays(daysAhead);
    rebuild(caretakerId, current, end);
  }


  /**
   * Rebuild Elastic slots for every date in [from <-> to] (inclusive).
   */
  @Async
  public void rebuildAsync(UUID caretakerId, LocalDate from, LocalDate to) {
  rebuild(caretakerId, from, to);
  }

  /**
   * Rebuild Elastic slots for every date in [from <-> to] (inclusive).
   */
  public void rebuild(UUID caretakerId, LocalDate from, LocalDate to) {
    Caretaker caretaker = caretakerRepository.findById(caretakerId).orElse(null);

    if (caretaker == null || from == null || to == null || from.isAfter(to)) {
      log.warn("CaretakerSlotsRebuildTrigger called with invalid parameters, {}, {}, {}", caretaker, from, to);
      return;
    }
    List<CaretakerRRule> rrules =
        rruleRepository.findAllByCaretaker_CaretakerIdAndIsEnabledTrue(caretaker.getCaretakerId());

    LocalDate cursor = from;
    while (!cursor.isAfter(to)) {
      rebuildSingleDay(caretaker, rrules, cursor);
      cursor = cursor.plusDays(1);
    }
  }

  private void rebuildSingleDay(Caretaker caretaker, List<CaretakerRRule> rrules,
      LocalDate date) {
    List<BookingInput> bookings = collectBookings(caretaker.getCaretakerId(), date);
    List<ElasticSlotDocument> docs =
        slotGenerationService.generateSlotsForDay(date, rrules, bookings, caretaker);
    slotCrudService.replaceSlotsByCaretakerAndDate(
        caretaker.getCaretakerId().toString(), date, docs);
    log.debug("Rebuilt {} elastic slot docs for caretaker {} on {}",
        docs.size(), caretaker.getCaretakerId(), date);
  }

  private List<BookingInput> collectBookings(UUID caretakerId, LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
    List<Event> events = eventRepository.findApprovedOverlapping(caretakerId, dayStart, dayEnd);

    return events.stream()
        .map(e -> toBookingInput(e, dayStart, dayEnd))
        .toList();
  }

  private BookingInput toBookingInput(Event event, LocalDateTime dayStart,
      LocalDateTime dayEnd) {
    LocalDateTime fromClipped = event.getDatetimeFrom().isBefore(dayStart)
        ? dayStart : event.getDatetimeFrom();
    LocalDateTime toClipped = event.getDatetimeTo().isAfter(dayEnd)
        ? dayEnd : event.getDatetimeTo();
    int petCount = event.getPets() == null ? 1 : event.getPets().size();
    return new BookingInput(fromClipped, toClipped, petCount);
  }
}

