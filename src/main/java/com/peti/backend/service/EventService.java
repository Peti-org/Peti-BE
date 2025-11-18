package com.peti.backend.service;

import static com.peti.backend.service.CaretakerService.convertToSimpleDto;

import com.peti.backend.dto.EventDto;
import com.peti.backend.dto.PriceDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.EventRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventService {

  private final EventRepository eventRepository;
  private final EntityManager entityManager;


  public static EventDto convertToDto(Event event) {
    return new EventDto(
        event.getEventId(),
        convertToSimpleDto(event.getCaretaker()),
        UserService.convertToDto(event.getUser(), CityService.convertToDto(event.getUser().getCityByCityId())),
        SlotService.convertToDto(event.getSlot()),
        event.getPrice(),
        event.getStatus()
    );
  }

  public EventDto createEvent(SlotDto slotDto, UUID userId) {
    // create event
    Event event = toEvent(slotDto, userId);
    Event saved = eventRepository.save(event);
    return convertToDto(saved);
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

  private Event toEvent(SlotDto slotDto, UUID userId) {
    Event event = new Event();

    event.setCaretaker(entityManager.getReference(Caretaker.class, slotDto.caretaker().getId()));
    event.setUser(entityManager.getReference(User.class, userId));
    event.setSlot(entityManager.getReference(Slot.class, slotDto.slotId()));
    event.setPrice(new PriceDto(slotDto.price(), slotDto.currency(), new ArrayList<>()));
    event.setStatus("created");
    event.setCreatedAt(LocalDateTime.now());
    event.setEventIsDeleted(false);

    return event;
  }
}
