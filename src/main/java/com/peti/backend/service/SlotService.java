package com.peti.backend.service;

import static com.peti.backend.service.CaretakerService.convertToSimpleDto;

import com.peti.backend.dto.slot.PagedSlotsResponse;
import com.peti.backend.dto.slot.RequestCalendarSlots;
import com.peti.backend.dto.slot.RequestSlotDto;
import com.peti.backend.dto.slot.RequestSlotFilters;
import com.peti.backend.dto.slot.SlotCursor;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.repository.SlotRepository;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * SlotService is responsible for managing slots and providing methods to retrieve and filter them.
 */
@Service
@RequiredArgsConstructor
public class SlotService {

  private final SlotRepository slotRepository;
  private final EntityManager entityManager;

  public static SlotDto convertToDto(Slot slot) {
    return new SlotDto(
        slot.getSlotId(),
        convertToSimpleDto(slot.getCaretaker()),
        slot.getDate().toLocalDate(),
        slot.getTimeFrom().toLocalTime(),
        slot.getTimeTo().toLocalTime(),
        slot.getType(),
        slot.getPrice(),
        slot.getCurrency()
    );
  }

  public PagedSlotsResponse getFilteredSlots(RequestSlotFilters requestSlotFilters) {

    List<Slot> slots = slotRepository.findSlotsWithCursor(requestSlotFilters.slotFilters(),
        requestSlotFilters.slotCursor());

    List<SlotDto> slotDtoList = slots.stream()
        .map(SlotService::convertToDto)
        .collect(Collectors.toList());

    if (slotDtoList.isEmpty()) {
      return new PagedSlotsResponse(List.of(), requestSlotFilters.slotCursor());
    }

    SlotCursor cursor = new SlotCursor(slotDtoList.getLast().caretaker().getRating(), slots.getLast().getCreationTime(),
        requestSlotFilters.slotCursor().limit());
    return new PagedSlotsResponse(slotDtoList, cursor);
  }

  public List<SlotDto> getCalendarSlots(RequestCalendarSlots requestCalendarSlots) {

    List<Slot> slots = slotRepository.findAllByCaretaker_CaretakerIdAndDateBetween(requestCalendarSlots.caretakerId(),
        Date.valueOf(requestCalendarSlots.fromDate()), Date.valueOf(requestCalendarSlots.toDate()));

    return slots.stream()
        .map(SlotService::convertToDto)
        .collect(Collectors.toList());
  }

  public Optional<SlotDto> getSlotById(UUID id) {
    return slotRepository.findById(id).map(SlotService::convertToDto);
  }

  public List<SlotDto> getCaretakerSlots(UUID caretakerId) {
    List<Slot> slots = slotRepository.findAllByCaretaker_CaretakerId(caretakerId);

    return slots.stream()
        .map(SlotService::convertToDto)
        .collect(Collectors.toList());
  }

  public SlotDto createSlot(RequestSlotDto requestSlotDto, UUID caretakerId) {
    Slot slot = toSlot(requestSlotDto, caretakerId);
    Slot saved = slotRepository.save(slot);
    return convertToDto(saved);
  }

  public Optional<SlotDto> updateSlot(UUID slotId, RequestSlotDto requestSlotDto, UUID caretakerId) {
    return slotRepository.findById(slotId)
        .filter(slot -> slot.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(existing -> {
          updateSlotEntity(existing, requestSlotDto);
          Slot saved = slotRepository.save(existing);
          return convertToDto(saved);
        });
  }

  public Optional<SlotDto> deleteSlot(UUID slotId, UUID caretakerId) {
    return slotRepository.findById(slotId)
        .filter(slot -> slot.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(slot -> {
          slotRepository.deleteById(slotId);
          return convertToDto(slot);
        });
  }

  private Slot toSlot(RequestSlotDto request, UUID caretakerId) {
    Slot slot = new Slot();

    slot.setCaretaker(entityManager.getReference(Caretaker.class, caretakerId));
    slot.setDate(Date.valueOf(request.date()));
    slot.setTimeFrom(Time.valueOf(request.timeFrom()));
    slot.setTimeTo(Time.valueOf(request.timeTo()));
    slot.setType(request.type());
    slot.setPrice(request.price());
    slot.setCurrency("UAH");//todo Hardcoded for now, consider making it dynamic
    slot.setCreationTime(LocalDateTime.now());
    slot.setAdditionalData("{\"test\": \"test\"}");

    return slot;
  }

  private void updateSlotEntity(Slot slot, RequestSlotDto request) {
    slot.setDate(Date.valueOf(request.date()));
    slot.setTimeFrom(Time.valueOf(request.timeFrom()));
    slot.setTimeTo(Time.valueOf(request.timeTo()));
    slot.setType(request.type());
    slot.setPrice(request.price());
    //todo think about updating time and what user will see if slot was updated
  }
}
