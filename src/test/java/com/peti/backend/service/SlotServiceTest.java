package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.slot.RequestSlotDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.internal.TimeSlotPair;
import com.peti.backend.repository.SlotRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SlotServiceTest {

  private SlotRepository slotRepository;
  private EntityManager entityManager;
  private SlotDivider slotDivider;
  private SlotService slotService;

  @BeforeEach
  void setUp() {
    slotRepository = mock(SlotRepository.class);
    entityManager = mock(EntityManager.class);
    slotDivider = mock(SlotDivider.class);
    slotService = new SlotService(slotRepository, entityManager, slotDivider);
  }

  @Test
  void testCreateSlot_DividesAndSavesSlots() {
    RequestSlotDto request = ResourceLoader.loadResource("slot-create-request.json", RequestSlotDto.class);
    Caretaker caretaker = ResourceLoader.loadResource("caretaker-entity.json", Caretaker.class);
    UUID caretakerId = caretaker.getCaretakerId();

    List<TimeSlotPair> pairs = ResourceLoader.loadResource("slot-divider-pairs.json", new TypeReference<>() {});
    when(slotDivider.divideTimeRange(any(), any(), anyInt())).thenReturn(pairs);
    when(entityManager.getReference(Caretaker.class, caretakerId)).thenReturn(caretaker);

    List<Slot> slots = ResourceLoader.loadResource("slot-entities.json", new TypeReference<>() {});
    when(slotRepository.saveAll(anyList())).thenReturn(slots);

    List<SlotDto> result = slotService.createSlot(request, caretakerId);

    assertEquals(slots.size(), result.size());
    assertEquals(request.type(), result.get(0).type());
    verify(slotRepository).saveAll(anyList());
  }

  @Test
  void testUpdateSlot_Success() {
    Slot slot = ResourceLoader.loadResource("slot-entity.json", Slot.class);
    Caretaker caretaker = ResourceLoader.loadResource("caretaker-entity.json", Caretaker.class);
    UUID slotId = slot.getSlotId();
    UUID caretakerId = caretaker.getCaretakerId();

    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
    when(slotRepository.save(any(Slot.class))).thenAnswer(invocation -> invocation.getArgument(0));

    RequestSlotDto updateDto = ResourceLoader.loadResource("slot-update-request.json", RequestSlotDto.class);

    Optional<SlotDto> updated = slotService.updateSlot(slotId, updateDto, caretakerId);

    assertTrue(updated.isPresent());
    assertEquals(updateDto.type(), updated.get().type());
    verify(slotRepository).save(any(Slot.class));
  }

  @Test
  void testUpdateSlot_WrongCaretaker() {
    Slot slot = ResourceLoader.loadResource("slot-entity.json", Slot.class);
    UUID slotId = slot.getSlotId();
    UUID wrongCaretakerId = UUID.randomUUID();

    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    RequestSlotDto updateDto = ResourceLoader.loadResource("slot-update-request.json", RequestSlotDto.class);

    Optional<SlotDto> updated = slotService.updateSlot(slotId, updateDto, wrongCaretakerId);

    assertTrue(updated.isEmpty());
    verify(slotRepository, never()).save(any());
  }

  @Test
  void testDeleteSlot_Success() {
    Slot slot = ResourceLoader.loadResource("slot-entity.json", Slot.class);
    UUID slotId = slot.getSlotId();
    UUID caretakerId = slot.getCaretaker().getCaretakerId();

    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    Optional<SlotDto> deleted = slotService.deleteSlot(slotId, caretakerId);

    assertTrue(deleted.isPresent());
    verify(slotRepository).deleteById(slotId);
  }

  @Test
  void testDeleteSlot_WrongCaretaker() {
    Slot slot = ResourceLoader.loadResource("slot-entity.json", Slot.class);
    UUID slotId = slot.getSlotId();
    UUID wrongCaretakerId = UUID.randomUUID();

    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    Optional<SlotDto> deleted = slotService.deleteSlot(slotId, wrongCaretakerId);

    assertTrue(deleted.isEmpty());
    verify(slotRepository, never()).deleteById(any());
  }
}
