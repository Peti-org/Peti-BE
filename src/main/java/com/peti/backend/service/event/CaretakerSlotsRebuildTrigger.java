package com.peti.backend.service.event;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.service.elastic.ElasticSlotCrudService;
import com.peti.backend.service.elastic.SlotGenerationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class CaretakerSlotsRebuildTrigger {

  private final CaretakerRRuleRepository rruleRepository;
  private final EventRepository eventRepository;
  private final SlotGenerationService slotGenerationService;
  private final ElasticSlotCrudService elasticSlotCrudService;

  /**
   * Rebuild Elastic slots for every date in [from..to] (inclusive).
   */
  public void rebuild(Caretaker caretaker, LocalDate from, LocalDate to) {
    if (caretaker == null || from == null || to == null || from.isAfter(to)) {
      log.debug("CaretakerSlotsRebuildTrigger called with invalid parameters, {}, {}, {}", caretaker, from, to);
      return;
    }
    List<CaretakerRRule> rrules =
        rruleRepository.findAllByCaretaker_CaretakerId(caretaker.getCaretakerId());

    LocalDate cursor = from;
    while (!cursor.isAfter(to)) {
      rebuildSingleDay(caretaker, rrules, cursor);
      cursor = cursor.plusDays(1);
    }
  }

  //todo refactor it to generate right Elsatic slots
  private void rebuildSingleDay(Caretaker caretaker, List<CaretakerRRule> rrules,
      LocalDate date) {
    List<BookingInput> bookings = collectBookings(caretaker.getCaretakerId(), date);
    List<ElasticSlotDocument> docs =
        slotGenerationService.generateSlotsForDay(date, rrules, bookings, caretaker);
    elasticSlotCrudService.replaceSlotsByCaretakerAndDate(
        caretaker.getCaretakerId().toString(), date, docs);
    log.debug("Rebuilt {} slot docs for caretaker {} on {}",
        docs.size(), caretaker.getCaretakerId(), date);
  }

  private List<BookingInput> collectBookings(UUID caretakerId, LocalDate date) {
    LocalDateTime dayStart = date.atStartOfDay();
    LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
    List<Event> events = eventRepository.findActiveOverlapping(caretakerId, dayStart, dayEnd);

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
    LocalTime timeFrom = fromClipped.toLocalTime();
    LocalTime timeTo = toClipped.equals(dayEnd) ? LocalTime.of(23, 59, 59)
        : toClipped.toLocalTime();
    int petCount = event.getPets() == null ? 1 : event.getPets().size();
    return new BookingInput(timeFrom, timeTo, petCount);
  }
}

