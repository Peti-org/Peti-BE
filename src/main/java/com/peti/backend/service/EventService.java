package com.peti.backend.service;

import com.peti.backend.dto.PriceDto;
import com.peti.backend.dto.event.EventDto;
import com.peti.backend.dto.event.RequestEventDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.EventRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final EntityManager entityManager;


  public static EventDto convertToDto(Event event) {
    return new EventDto(
        event.getEventId(),
        event.getCaretaker().getCaretakerId(),
        event.getUser().getUserId(),
        event.getPrice(),
        event.getStatus()
    );
  }

  public static EventDto convertToDto(Event event, UUID caretakerId, UUID userId) {
    return new EventDto(
        event.getEventId(),
        caretakerId,
        userId,
        event.getPrice(),
        event.getStatus()
    );
  }

  @Transactional
  public EventDto createEvent(RequestEventDto requestEventDto, UUID userId) {
    List<Pet> pets = getUserPets(requestEventDto.petsIds(), userId);
    List<Slot> slots = getAndValidateSlots(requestEventDto.slotsIds());
    UUID caretakerId = validateSlotsBelongToSameCaretaker(slots);
    validateSlotsAreConsecutive(slots);
    validateSlotsCapacity(slots, pets.size());
    incrementSlotsOccupiedCapacity(slots, pets.size());

    Event event = buildEvent(slots, pets, caretakerId, userId);
    Event saved = eventRepository.save(event);

    return convertToDto(saved, caretakerId, userId);
  }

  public List<EventDto> getEventsByCaretakerId(UUID caretakerId) {
    List<Event> events = eventRepository.findAllByCaretaker_CaretakerId(caretakerId);

    return events.stream()
        .map(EventService::convertToDto)
        .collect(Collectors.toList());
  }

  public List<EventDto> getEventsByUserId(UUID userId) {
    List<Event> events = eventRepository.findAllByUser_UserId(userId);

    return events.stream()
        .map(EventService::convertToDto)
        .collect(Collectors.toList());
  }

  public boolean deleteEvent(UUID eventId, UUID userId) {
    if (eventRepository.existsByEventIdAndUser_UserId(eventId, userId)) {
      eventRepository.deleteById(eventId);
      return true;
    }
    return false;
  }

  private List<Pet> getUserPets(List<UUID> petIds, UUID userId) {
    List<Pet> pets = entityManager.createQuery(
            "SELECT p FROM Pet p WHERE p.petId IN :petIds AND p.petOwner.userId = :userId", Pet.class)
        .setParameter("petIds", petIds)
        .setParameter("userId", userId)
        .getResultList();
    if (pets.size() != petIds.size()) {
      throw new IllegalArgumentException("Some pets do not belong to the user");
    }
    return pets;
  }

  private List<Slot> getAndValidateSlots(List<UUID> slotIds) {
    List<Slot> slots = entityManager.createQuery(
            "SELECT s FROM Slot s WHERE s.slotId IN :slotIds", Slot.class)
        .setParameter("slotIds", slotIds)
        .getResultList();
    if (slots.size() != slotIds.size()) {
      throw new IllegalArgumentException("Some slots do not exist");
    }
    return slots;
  }

  private UUID validateSlotsBelongToSameCaretaker(List<Slot> slots) {
    UUID caretakerId = slots.getFirst().getCaretaker().getCaretakerId();
    boolean allSameCaretaker = slots.stream()
        .allMatch(s -> s.getCaretaker().getCaretakerId().equals(caretakerId));
    if (!allSameCaretaker) {
      throw new IllegalArgumentException("Slots must belong to the same caretaker");
    }
    return caretakerId;
  }

  private void validateSlotsAreConsecutive(List<Slot> slots) {
    slots.sort(Comparator.comparing(Slot::getDate).thenComparing(Slot::getTimeFrom));
    for (int i = 1; i < slots.size(); i++) {
      Slot prev = slots.get(i - 1);
      Slot curr = slots.get(i);
      if (!prev.getDate().equals(curr.getDate()) ||
          !prev.getTimeTo().equals(curr.getTimeFrom())) {
        throw new IllegalArgumentException("Slots are not consecutive");
      }
    }
  }

  private void validateSlotsCapacity(List<Slot> slots, int petsCount) {
    for (Slot slot : slots) {
      if (slot.getCapacity() - slot.getOccupiedCapacity() < petsCount) {
        throw new IllegalArgumentException("Slot " + slot.getSlotId() + " does not have enough capacity");
      }
    }
  }

  private void incrementSlotsOccupiedCapacity(List<Slot> slots, int petsCount) {
    for (Slot slot : slots) {
      slot.setOccupiedCapacity(slot.getOccupiedCapacity() + petsCount);
      entityManager.persist(slot);
    }
  }

  private Event buildEvent(List<Slot> slots, List<Pet> pets, UUID caretakerId, UUID userId) {
    Event event = new Event();
    event.setCaretaker(entityManager.getReference(Caretaker.class, caretakerId));
    event.setUser(entityManager.getReference(User.class, userId));
    event.setPrice(new PriceDto(BigDecimal.ONE, "UAH", new ArrayList<>()));
    event.setStatus("created");
    event.setCreatedAt(LocalDateTime.now());
    event.setEventIsDeleted(false);
    event.setType("walk");
    event.setDatetimeFrom(slots.getFirst().getDate().toLocalDate()
        .atTime(slots.getFirst().getTimeFrom().toLocalTime()));
    Slot lastSlot = slots.getLast();
    event.setDatetimeTo(lastSlot.getDate().toLocalDate()
        .atTime(lastSlot.getTimeTo().toLocalTime()));
    event.getSlots().addAll(slots);
    event.getPets().addAll(pets);
    return event;
  }

  private Event toEvent(SlotDto slotDto, UUID userId) {
    Event event = new Event();

    event.setCaretaker(entityManager.getReference(Caretaker.class, slotDto.caretaker().getId()));
    event.setUser(entityManager.getReference(User.class, userId));
//    event.setSlot(entityManager.getReference(Slot.class, slotDto.slotId()));
    event.setPrice(new PriceDto(slotDto.price(), slotDto.currency(), new ArrayList<>()));
    event.setStatus("created");
    event.setCreatedAt(LocalDateTime.now());
    event.setEventIsDeleted(false);

    return event;
  }
}
