package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.event.EventDto;
import com.peti.backend.dto.event.RequestEventDto;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.EventStatus;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.repository.PetRepository;
import com.peti.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID CARETAKER_ID =
      UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");
  private static final UUID RRULE_ID = UUID.fromString("11111111-aaaa-1111-aaaa-111111111111");
  private static final UUID EVENT_ID = UUID.fromString("eeee2222-2222-2222-2222-222222222222");
  private static final UUID PET_A = UUID.fromString("aaaaaaaa-1111-1111-1111-111111111111");
  private static final UUID PET_B = UUID.fromString("bbbbbbbb-2222-2222-2222-222222222222");

  private static final LocalDateTime FROM = LocalDateTime.of(2026, 5, 2, 10, 0);
  private static final LocalDateTime TO = LocalDateTime.of(2026, 5, 2, 11, 0);

  @Mock private EventRepository eventRepository;
  @Mock private CaretakerRRuleRepository rruleRepository;
  @Mock private UserRepository userRepository;
  @Mock private PetRepository petRepository;
  @Mock private EventValidator validator;
  @Mock private EventPriceCalculator priceCalculator;
  @Mock private CaretakerSlotsRebuildTrigger slotsRebuildTrigger;

  @InjectMocks
  private EventService eventService;

  // ---------- createEvent ----------

  @Test
  @DisplayName("createEvent - success returns mapped DTO and triggers slot rebuild")
  void createEvent_success() {
    CaretakerRRule rrule = ResourceLoader.loadResource(
        "rrule-for-event-entity.json", CaretakerRRule.class);
    User user = ResourceLoader.loadResource("user-entity.json", User.class);
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});
    EventDto expected =
        ResourceLoader.loadResource("event-created-response.json", EventDto.class);

    when(rruleRepository.findById(RRULE_ID)).thenReturn(Optional.of(rrule));
    when(petRepository.findAllByPetIdInAndPetOwner_UserId(anyList(), eq(USER_ID))).thenReturn(pets);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    when(priceCalculator.calculate(rrule.getCaretaker(), "WALKING", pets))
        .thenReturn(expected.price());
    when(eventRepository.save(any(Event.class))).thenAnswer(inv -> {
      Event e = inv.getArgument(0);
      e.setEventId(EVENT_ID);
      return e;
    });

    RequestEventDto request = new RequestEventDto(RRULE_ID, FROM, TO, List.of(PET_A, PET_B));
    EventDto result = eventService.createEvent(request, USER_ID);

    assertThat(result).usingRecursiveComparison().isEqualTo(expected);
    verify(slotsRebuildTrigger).rebuild(rrule.getCaretaker(),
        FROM.toLocalDate(), TO.toLocalDate());
    verify(validator).validatePetOwnership(pets, List.of(PET_A, PET_B), USER_ID);
  }

  @Test
  @DisplayName("createEvent - rrule not found throws NotFoundException, no rebuild")
  void createEvent_rruleNotFound() {
    when(rruleRepository.findById(RRULE_ID)).thenReturn(Optional.empty());

    RequestEventDto request = new RequestEventDto(RRULE_ID, FROM, TO, List.of(PET_A));
    assertThatThrownBy(() -> eventService.createEvent(request, USER_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("RRule not found");

    verify(slotsRebuildTrigger, never()).rebuild(any(), any(), any());
    verify(eventRepository, never()).save(any());
  }

  @Test
  @DisplayName("createEvent - user not found throws NotFoundException after validation passes")
  void createEvent_userNotFound() {
    CaretakerRRule rrule = ResourceLoader.loadResource(
        "rrule-for-event-entity.json", CaretakerRRule.class);
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    when(rruleRepository.findById(RRULE_ID)).thenReturn(Optional.of(rrule));
    when(petRepository.findAllByPetIdInAndPetOwner_UserId(anyList(), eq(USER_ID))).thenReturn(pets);
    when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

    RequestEventDto request = new RequestEventDto(RRULE_ID, FROM, TO, List.of(PET_A, PET_B));
    assertThatThrownBy(() -> eventService.createEvent(request, USER_ID))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("User not found");
  }

  // ---------- getEventsByCaretakerId ----------

  @Test
  @DisplayName("getEventsByCaretakerId - excludes DELETED via repository filter")
  void getEventsByCaretakerId_excludesDeleted() {
    when(eventRepository
        .findAllByCaretaker_CaretakerIdAndStatusNot(CARETAKER_ID, EventStatus.DELETED))
        .thenReturn(List.of());

    List<EventDto> result = eventService.getEventsByCaretakerId(CARETAKER_ID);

    assertThat(result).isEmpty();
    verify(eventRepository, times(1))
        .findAllByCaretaker_CaretakerIdAndStatusNot(CARETAKER_ID, EventStatus.DELETED);
  }

  @Test
  @DisplayName("getEventsByUserId - excludes DELETED via repository filter")
  void getEventsByUserId_excludesDeleted() {
    when(eventRepository.findAllByUser_UserIdAndStatusNot(USER_ID, EventStatus.DELETED))
        .thenReturn(List.of());

    List<EventDto> result = eventService.getEventsByUserId(USER_ID);

    assertThat(result).isEmpty();
    verify(eventRepository).findAllByUser_UserIdAndStatusNot(USER_ID, EventStatus.DELETED);
  }

  // ---------- deleteEvent ----------

  @Test
  @DisplayName("deleteEvent - owner soft-deletes, status becomes DELETED, triggers rebuild")
  void deleteEvent_ownerSoftDeletes() {
    Event event = ResourceLoader.loadResource("event-active-entity.json", Event.class);
    when(eventRepository.findByEventIdAndStatusNot(EVENT_ID, EventStatus.DELETED))
        .thenReturn(Optional.of(event));
    when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

    boolean result = eventService.deleteEvent(EVENT_ID, USER_ID);

    assertThat(result).isTrue();
    ArgumentCaptor<Event> savedCaptor = ArgumentCaptor.forClass(Event.class);
    verify(eventRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getStatus()).isEqualTo(EventStatus.DELETED);
    verify(slotsRebuildTrigger).rebuild(event.getCaretaker(),
        event.getDatetimeFrom().toLocalDate(), event.getDatetimeTo().toLocalDate());
  }

  @Test
  @DisplayName("deleteEvent - non-owner returns false, no save and no rebuild")
  void deleteEvent_nonOwner() {
    Event event = ResourceLoader.loadResource("event-active-entity.json", Event.class);
    when(eventRepository.findByEventIdAndStatusNot(EVENT_ID, EventStatus.DELETED))
        .thenReturn(Optional.of(event));

    UUID otherUser = UUID.randomUUID();
    boolean result = eventService.deleteEvent(EVENT_ID, otherUser);

    assertThat(result).isFalse();
    verify(eventRepository, never()).save(any());
    verify(slotsRebuildTrigger, never()).rebuild(any(), any(), any());
  }

  @Test
  @DisplayName("deleteEvent - missing event returns false, no save and no rebuild")
  void deleteEvent_notFound() {
    when(eventRepository.findByEventIdAndStatusNot(EVENT_ID, EventStatus.DELETED))
        .thenReturn(Optional.empty());

    boolean result = eventService.deleteEvent(EVENT_ID, USER_ID);

    assertThat(result).isFalse();
    verify(slotsRebuildTrigger, never()).rebuild(any(), any(), any());
  }
}

