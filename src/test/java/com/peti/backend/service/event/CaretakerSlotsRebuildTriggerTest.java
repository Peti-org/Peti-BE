package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.internal.EventStatus;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.service.elastic.ElasticSlotCrudService;
import com.peti.backend.service.elastic.SlotGenerationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaretakerSlotsRebuildTriggerTest {

  private static final UUID CARETAKER_ID =
      UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");

  @Mock private CaretakerRRuleRepository rruleRepository;
  @Mock private EventRepository eventRepository;
  @Mock private SlotGenerationService slotGenerationService;
  @Mock private ElasticSlotCrudService elasticSlotCrudService;

  @InjectMocks
  private CaretakerSlotsRebuildTrigger trigger;

  @Test
  @DisplayName("rebuild - iterates each day in [from..to] and replaces docs")
  void rebuild_iteratesAllDays() {
    Caretaker caretaker = caretakerRef();
    LocalDate from = LocalDate.of(2026, 5, 1);
    LocalDate to = LocalDate.of(2026, 5, 3);

    when(rruleRepository.findAllByCaretaker_CaretakerId(CARETAKER_ID))
        .thenReturn(List.of());
    when(eventRepository.findActiveOverlapping(eq(CARETAKER_ID), any(), any()))
        .thenReturn(List.of());
    when(slotGenerationService.generateSlotsForDay(any(), any(), any(), any()))
        .thenReturn(List.of());

    trigger.rebuild(caretaker, from, to);

    verify(elasticSlotCrudService, times(3))
        .replaceSlotsByCaretakerAndDate(eq(CARETAKER_ID.toString()), any(), any());
  }

  @Test
  @DisplayName("rebuild - converts active events into BookingInputs (one per event with petCount)")
  void rebuild_buildsBookingsFromEvents() {
    Caretaker caretaker = caretakerRef();
    LocalDate day = LocalDate.of(2026, 5, 2);

    Event event = new Event();
    event.setStatus(EventStatus.CREATED);
    event.setDatetimeFrom(LocalDateTime.of(2026, 5, 2, 10, 0));
    event.setDatetimeTo(LocalDateTime.of(2026, 5, 2, 12, 0));
    event.setPets(new java.util.HashSet<>(java.util.List.of(
        petWithId(UUID.randomUUID()),
        petWithId(UUID.randomUUID()))));

    when(rruleRepository.findAllByCaretaker_CaretakerId(CARETAKER_ID))
        .thenReturn(List.of());
    when(eventRepository.findActiveOverlapping(eq(CARETAKER_ID), any(), any()))
        .thenReturn(List.of(event));
    when(slotGenerationService.generateSlotsForDay(any(), any(), any(), any()))
        .thenReturn(List.of());

    trigger.rebuild(caretaker, day, day);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<BookingInput>> bookingsCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(slotGenerationService).generateSlotsForDay(
        eq(day), any(), bookingsCaptor.capture(), eq(caretaker));

    assertThat(bookingsCaptor.getValue()).hasSize(1);
    BookingInput b = bookingsCaptor.getValue().get(0);
    assertThat(b.bookedCapacity()).isEqualTo(2);
    assertThat(b.timeFrom()).isEqualTo(java.time.LocalTime.of(10, 0));
    assertThat(b.timeTo()).isEqualTo(java.time.LocalTime.of(12, 0));
  }

  @Test
  @DisplayName("rebuild - persists generated docs via replaceSlotsByCaretakerAndDate")
  void rebuild_persistsGeneratedDocs() {
    Caretaker caretaker = caretakerRef();
    LocalDate day = LocalDate.of(2026, 5, 2);

    when(rruleRepository.findAllByCaretaker_CaretakerId(CARETAKER_ID))
        .thenReturn(List.of(new CaretakerRRule()));
    when(eventRepository.findActiveOverlapping(eq(CARETAKER_ID), any(), any()))
        .thenReturn(List.of());
    when(slotGenerationService.generateSlotsForDay(any(), any(), any(), any()))
        .thenReturn(List.of(new ElasticSlotDocument()));

    trigger.rebuild(caretaker, day, day);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<ElasticSlotDocument>> docsCaptor =
        ArgumentCaptor.forClass(List.class);
    verify(elasticSlotCrudService).replaceSlotsByCaretakerAndDate(
        eq(CARETAKER_ID.toString()), eq(day), docsCaptor.capture());
    assertThat(docsCaptor.getValue()).hasSize(1);
  }

  @Test
  @DisplayName("rebuild - null caretaker is a no-op")
  void rebuild_nullCaretaker() {
    trigger.rebuild(null, LocalDate.now(), LocalDate.now());

    verify(elasticSlotCrudService, never())
        .replaceSlotsByCaretakerAndDate(anyString(), any(), any());
  }

  @Test
  @DisplayName("rebuild - from after to is a no-op")
  void rebuild_fromAfterTo() {
    trigger.rebuild(caretakerRef(),
        LocalDate.of(2026, 5, 5), LocalDate.of(2026, 5, 1));

    verify(elasticSlotCrudService, never())
        .replaceSlotsByCaretakerAndDate(anyString(), any(), any());
  }

  private Caretaker caretakerRef() {
    Caretaker c = new Caretaker();
    c.setCaretakerId(CARETAKER_ID);
    return c;
  }

  private com.peti.backend.model.domain.Pet petWithId(UUID id) {
    com.peti.backend.model.domain.Pet p = new com.peti.backend.model.domain.Pet();
    p.setPetId(id);
    return p;
  }
}

