package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
public class CaretakerRRuleServiceTest {

  @Mock
  private CaretakerRRuleRepository rruleRepository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private SlotGenerationScheduler slotGenerationScheduler;

  @InjectMocks
  private CaretakerRRuleService rruleService;

  private UUID caretakerId;
  private UUID rruleId;
  private CaretakerRRule rrule;
  private RequestRRuleDto requestRRuleDto;
  private Caretaker caretaker;

  @BeforeEach
  void setUp() {
    caretakerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    rruleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    // Load test data
    requestRRuleDto = ResourceLoader.loadResource("rrule-create-request.json", RequestRRuleDto.class);

    // Create mock entities
    caretaker = new Caretaker();
    caretaker.setCaretakerId(caretakerId);

    rrule = new CaretakerRRule();
    rrule.setRruleId(rruleId);
    rrule.setCaretaker(caretaker);
    rrule.setRrule(requestRRuleDto.rrule());
    rrule.setDtstart(requestRRuleDto.dtstart());
    rrule.setDtend(requestRRuleDto.dtend());
    rrule.setDescription(requestRRuleDto.description());
    rrule.setSlotType(requestRRuleDto.slotType());
    rrule.setCapacity(requestRRuleDto.capacity());
    rrule.setIntervalMinutes(requestRRuleDto.intervalMinutes());
    rrule.setIsEnabled(requestRRuleDto.isEnabled());
    rrule.setIsSchedule(requestRRuleDto.isSchedule());
    rrule.setIsBusy(requestRRuleDto.isBusy());
    rrule.setPriority(requestRRuleDto.priority());
  }

  @Test
  public void testCreateRRule_Success() {
    when(entityManager.getReference(Caretaker.class, caretakerId)).thenReturn(caretaker);
    when(rruleRepository.save(any(CaretakerRRule.class))).thenReturn(rrule);
    when(slotGenerationScheduler.generateSlotsForSingleRRule(any(), any(), any())).thenReturn(10);

    RRuleDto result = rruleService.createRRule(requestRRuleDto, caretakerId);

    assertNotNull(result);
    assertEquals(requestRRuleDto.rrule(), result.rrule());
    assertEquals(requestRRuleDto.capacity(), result.capacity());
    assertEquals(requestRRuleDto.intervalMinutes(), result.intervalMinutes());
    assertEquals(requestRRuleDto.isEnabled(), result.isEnabled());
    assertEquals(requestRRuleDto.isSchedule(), result.isSchedule());
    assertEquals(requestRRuleDto.isBusy(), result.isBusy());
    assertEquals(requestRRuleDto.priority(), result.priority());
    verify(rruleRepository).save(any(CaretakerRRule.class));
    verify(slotGenerationScheduler).generateSlotsForSingleRRule(any(), any(), any());
  }

  @Test
  public void testGetAllRRulesForCaretaker() {
    List<CaretakerRRule> rrules = Arrays.asList(rrule);
    when(rruleRepository.findAllByCaretaker_CaretakerId(caretakerId)).thenReturn(rrules);

    List<RRuleDto> result = rruleService.getAllRRulesForCaretaker(caretakerId);

    assertEquals(1, result.size());
    assertEquals(rruleId, result.get(0).rruleId());
  }

  @Test
  public void testUpdateRRule_Success() {
    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(rrule));
    when(rruleRepository.save(any(CaretakerRRule.class))).thenReturn(rrule);
    when(slotGenerationScheduler.deleteSlotsForRRule(any(), any())).thenReturn(5);
    when(slotGenerationScheduler.generateSlotsForSingleRRule(any(), any(), any())).thenReturn(10);

    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, requestRRuleDto, caretakerId);

    assertTrue(result.isPresent());
    verify(slotGenerationScheduler).deleteSlotsForRRule(eq(rruleId), any(LocalDate.class));
    verify(slotGenerationScheduler).generateSlotsForSingleRRule(eq(rruleId), any(), any());
    verify(rruleRepository).save(any(CaretakerRRule.class));
  }

  @Test
  public void testUpdateRRule_WrongCaretaker() {
    UUID differentCaretakerId = UUID.randomUUID();
    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(rrule));

    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, requestRRuleDto, differentCaretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  public void testDeleteRRule_Success() {
    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(rrule));
    when(slotGenerationScheduler.deleteSlotsForRRule(any(), any())).thenReturn(5);

    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, caretakerId);

    assertTrue(result.isPresent());
    verify(slotGenerationScheduler).deleteSlotsForRRule(eq(rruleId), any(LocalDate.class));
    verify(rruleRepository).deleteById(rruleId);
  }

  @Test
  public void testDeleteRRule_WrongCaretaker() {
    UUID differentCaretakerId = UUID.randomUUID();
    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(rrule));

    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, differentCaretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  public void testConvertToDto() {
    rrule.setGeneratedTo(LocalDate.of(2026, 2, 17));

    RRuleDto result = CaretakerRRuleService.convertToDto(rrule);

    assertNotNull(result);
    assertEquals(rruleId, result.rruleId());
    assertEquals(requestRRuleDto.rrule(), result.rrule());
    assertEquals(requestRRuleDto.capacity(), result.capacity());
    assertEquals(requestRRuleDto.intervalMinutes(), result.intervalMinutes());
    assertEquals(requestRRuleDto.isEnabled(), result.isEnabled());
    assertEquals(requestRRuleDto.isSchedule(), result.isSchedule());
    assertEquals(requestRRuleDto.isBusy(), result.isBusy());
    assertEquals(requestRRuleDto.priority(), result.priority());
  }
}

