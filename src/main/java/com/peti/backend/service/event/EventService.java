package com.peti.backend.service.event;

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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates event lifecycle: creation from {@link CaretakerRRule}, listing,
 * and soft-deletion (status set to DELETED). Triggers Elastic slot rebuilds
 * on every state change that affects caretaker availability.
 */
@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final CaretakerRRuleRepository rruleRepository;
  private final UserRepository userRepository;
  private final PetRepository petRepository;

  private final EventValidator validator;
  private final EventPriceCalculator priceCalculator;
  private final CaretakerSlotsRebuildTrigger slotsRebuildTrigger;

  @Transactional
  public EventDto createEvent(RequestEventDto request, UUID userId) {
    CaretakerRRule rrule = rruleRepository.findById(request.rruleId())
        .orElseThrow(() -> new NotFoundException("RRule not found: " + request.rruleId()));

    validator.validateTimeWindow(rrule, request.datetimeFrom(), request.datetimeTo());

    List<Pet> pets = petRepository.findAllByPetIdInAndPetOwner_UserId(request.petsIds(), userId);
    validator.validatePetOwnership(pets, request.petsIds(), userId);

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));

    Event event = buildEvent(rrule, pets, user, request);
    Event saved = eventRepository.save(event);

    slotsRebuildTrigger.rebuild(rrule.getCaretaker(),
        request.datetimeFrom().toLocalDate(),
        request.datetimeTo().toLocalDate());

    return EventDto.from(saved);
  }

  @Transactional(readOnly = true)
  public List<EventDto> getEventsByCaretakerId(UUID caretakerId) {
    return eventRepository
        .findAllByCaretaker_CaretakerIdAndStatusNot(caretakerId, EventStatus.DELETED)
        .stream()
        .map(EventDto::from)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<EventDto> getEventsByUserId(UUID userId) {
    return eventRepository
        .findAllByUser_UserIdAndStatusNot(userId, EventStatus.DELETED)
        .stream()
        .map(EventDto::from)
        .toList();
  }

  @Transactional
  public boolean deleteEvent(UUID eventId, UUID userId) {
    Event event = eventRepository
        .findByEventIdAndStatusNot(eventId, EventStatus.DELETED)
        .filter(e -> e.getUser().getUserId().equals(userId))
        .orElse(null);
    if (event == null) {
      return false;
    }
    event.setStatus(EventStatus.DELETED);
    eventRepository.save(event);

    slotsRebuildTrigger.rebuild(event.getCaretaker(),
        event.getDatetimeFrom().toLocalDate(),
        event.getDatetimeTo().toLocalDate());
    return true;
  }

  private Event buildEvent(CaretakerRRule rrule, List<Pet> pets, User user,
      RequestEventDto request) {
    Event event = new Event();
    event.setUser(user);
    event.setCaretaker(rrule.getCaretaker());
    event.setRrule(rrule);
    event.setStatus(EventStatus.CREATED);
    event.setType(rrule.getSlotType());
    event.setCreatedAt(LocalDateTime.now());
    event.setDatetimeFrom(request.datetimeFrom());
    event.setDatetimeTo(request.datetimeTo());
    event.setPrice(priceCalculator.calculate(rrule.getCaretaker(), rrule.getSlotType(), pets));
    event.getPets().addAll(pets);
    return event;
  }
}
