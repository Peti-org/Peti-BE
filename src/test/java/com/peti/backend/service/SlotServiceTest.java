package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.slot.RequestSlotDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.internal.TimeSlotPair;
import com.peti.backend.repository.SlotRepository;
import com.peti.backend.service.slot.SlotDivider;
import com.peti.backend.service.slot.SlotService;
import jakarta.persistence.EntityManager;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SlotServiceTest {

  @Mock
  private SlotRepository slotRepository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private SlotDivider slotDivider;

  @InjectMocks
  private SlotService slotService;

  private UUID caretakerId;
  private UUID slotId;
  private Slot slot;
  private RequestSlotDto requestSlotDto;

  @BeforeEach
  void setUp() {
    caretakerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    slotId = UUID.fromString("323e4567-e89b-12d3-a456-426614174003");

    // Load test data
    slot = ResourceLoader.loadResource("slot-entity.json", Slot.class);
    requestSlotDto = ResourceLoader.loadResource("slot-create-request.json", RequestSlotDto.class);
  }

  @Test
  public void testCreateSlot_DividesAndSavesSlots() {
    // Mock SlotDivider to return 4 slots (2 hours / 30 min)
    List<TimeSlotPair> timeSlots = Arrays.asList(
        new TimeSlotPair(LocalTime.of(9, 0), LocalTime.of(9, 30)),
        new TimeSlotPair(LocalTime.of(9, 30), LocalTime.of(10, 0)),
        new TimeSlotPair(LocalTime.of(10, 0), LocalTime.of(10, 30)),
        new TimeSlotPair(LocalTime.of(10, 30), LocalTime.of(11, 0))
    );
    when(slotDivider.divideTimeRange(any(), any(), anyInt())).thenReturn(timeSlots);
    when(entityManager.getReference(Caretaker.class, caretakerId)).thenReturn(slot.getCaretaker());
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    List<SlotDto> result = slotService.createSlot(requestSlotDto, caretakerId);

    assertEquals(4, result.size());
    verify(slotDivider).divideTimeRange(requestSlotDto.timeFrom(), requestSlotDto.timeTo(), 30);
    verify(slotRepository).saveAll(anyList());
  }

  @Test
  public void testGetSlotById_Found() {
    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    Optional<SlotDto> result = slotService.getSlotById(slotId);

    assertTrue(result.isPresent());
    assertEquals(slotId, result.get().slotId());
    assertEquals(false, result.get().isRepeated());
  }

  @Test
  public void testGetSlotById_NotFound() {
    when(slotRepository.findById(slotId)).thenReturn(Optional.empty());

    Optional<SlotDto> result = slotService.getSlotById(slotId);

    assertFalse(result.isPresent());
  }

  @Test
  public void testUpdateSlot_Success() {
    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
    when(slotRepository.save(any(Slot.class))).thenReturn(slot);

    Optional<SlotDto> result = slotService.updateSlot(slotId, requestSlotDto, caretakerId);

    assertTrue(result.isPresent());
    verify(slotRepository).save(any(Slot.class));
  }

  @Test
  public void testUpdateSlot_WrongCaretaker() {
    UUID differentCaretakerId = UUID.randomUUID();
    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    Optional<SlotDto> result = slotService.updateSlot(slotId, requestSlotDto, differentCaretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  public void testDeleteSlot_Success() {
    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));
    doNothing().when(slotRepository).deleteById(slotId);

    Optional<SlotDto> result = slotService.deleteSlot(slotId, caretakerId);

    assertTrue(result.isPresent());
    verify(slotRepository).deleteById(slotId);
  }

  @Test
  public void testDeleteSlot_WrongCaretaker() {
    UUID differentCaretakerId = UUID.randomUUID();
    when(slotRepository.findById(slotId)).thenReturn(Optional.of(slot));

    Optional<SlotDto> result = slotService.deleteSlot(slotId, differentCaretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  public void testGetCaretakerSlots() {
    List<Slot> slots = Arrays.asList(slot);
    when(slotRepository.findAllByCaretaker_CaretakerId(caretakerId)).thenReturn(slots);

    List<SlotDto> result = slotService.getCaretakerSlots(caretakerId);

    assertEquals(1, result.size());
    assertEquals(slotId, result.get(0).slotId());
  }

  @Test
  public void testConvertToDto_IncludesIsRepeated() {
    SlotDto result = SlotService.convertToDto(slot);

    assertNotNull(result);
    assertEquals(slotId, result.slotId());
    assertEquals(false, result.isRepeated());
    assertEquals(3, result.capacity()); // Available capacity
  }
}

