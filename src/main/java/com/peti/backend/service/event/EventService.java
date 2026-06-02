package com.peti.backend.service.event;

import com.peti.backend.dto.event.EventDto;
import com.peti.backend.dto.event.RequestEventDto;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.internal.EventStatus;
import com.peti.backend.repository.EventRepository;
import com.peti.backend.repository.PetRepository;
import com.peti.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.peti.backend.service.slot.SlotsRebuildTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orchestrates event lifecycle: creation, listing, and soft-deletion.
 * Triggers Elastic slot rebuilds on every state change that affects caretaker availability.
 */
@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final UserRepository userRepository;
  private final PetRepository petRepository;

  private final EventValidator validator;
  private final EventPriceCalculator priceCalculator;
  private final SlotsRebuildTrigger slotsRebuildTrigger;
  private final RRuleMatcher rruleMatcher;
  private final RRuleCapacityChecker capacityChecker;

  @Transactional
  public EventDto createEvent(RequestEventDto request, UUID userId) {
    validator.validateTimeOrder(request.datetimeFrom(), request.datetimeTo());

    List<CaretakerRRule> matchingRules = rruleMatcher.findMatchingRules(
        request.caretakerId(), request.slotType(),
        request.datetimeFrom(), request.datetimeTo());

    Caretaker caretaker = matchingRules.getFirst().getCaretaker();
    validator.validateDuration(caretaker, request.slotType(),
        request.datetimeFrom(), request.datetimeTo());

    List<Pet> pets = petRepository.findAllByPetIdInAndPetOwner_UserId(request.petsIds(), userId);
    validator.validatePetOwnership(pets, request.petsIds(), userId);

    capacityChecker.validateCapacity(matchingRules, request.caretakerId(),
        request.datetimeFrom(), request.datetimeTo(), pets.size());

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found: " + userId));

    Event event = buildEvent(caretaker, pets, user, request);
    Event saved = eventRepository.save(event);

    slotsRebuildTrigger.rebuildAsync(request.caretakerId(),
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

    slotsRebuildTrigger.rebuildAsync(event.getCaretaker().getCaretakerId(),
        event.getDatetimeFrom().toLocalDate(),
        event.getDatetimeTo().toLocalDate());
    return true;
  }

  private Event buildEvent(Caretaker caretaker, List<Pet> pets, User user,
      RequestEventDto request) {
    Event event = new Event();
    event.setUser(user);
    event.setCaretaker(caretaker);
    event.setStatus(EventStatus.CREATED);
    event.setType(request.slotType());
    event.setCreatedAt(LocalDateTime.now());
    event.setDatetimeFrom(request.datetimeFrom());
    event.setDatetimeTo(request.datetimeTo());
    event.setPrice(priceCalculator.calculate(caretaker, request.slotType(), pets));
    event.getPets().addAll(pets);
    return event;
  }
}

