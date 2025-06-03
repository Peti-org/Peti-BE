package com.peti.backend.service;

import com.peti.backend.dto.CaretakerSlotDto;
import com.peti.backend.model.CaretakerSlot;
import com.peti.backend.repository.CaretakerSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CaretakerSlotService {

  private final CaretakerSlotRepository slotRepository;

  public List<CaretakerSlotDto> getAllEvents() {
    List<CaretakerSlot> rawSlots = slotRepository.findAll();
    return rawSlots.stream()
            .map(CaretakerSlotDto::new)
            .collect(Collectors.toList());
  }

  public Optional<CaretakerSlotDto> getEventById(UUID id) {
    return slotRepository.findById(id).map(CaretakerSlotDto::new);
  }

  public CaretakerSlot createEvent(CaretakerSlot slot) {
    return slotRepository.save(slot);
  }

  //    public Optional<Event> updateEvent(Long id, Event eventDetails) {
  //        return eventRepository.findById(id).map(event -> {
  //            event.setName(eventDetails.getName());
  //            event.setDescription(eventDetails.getDescription());
  //            event.setDate(eventDetails.getDate());
  //            event.setLocation(eventDetails.getLocation());
  //            return eventRepository.save(event);
  //        });
  //    }

  //    public boolean deleteEvent(Long id) {
  //        if (eventRepository.existsById(id)) {
  //            eventRepository.deleteById(id);
  //            return true;
  //        }
  //        return false;
  //    }
}