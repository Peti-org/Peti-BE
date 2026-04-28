package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.SlotRepository;
import com.peti.backend.service.slot.SlotGenerationScheduler;
import com.peti.backend.service.slot.SlotGenerationScheduler.SlotGenerationResult;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.peti.backend.service.slot.RRuleSlotGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Disabled
@ExtendWith(MockitoExtension.class)
public class SlotGenerationSchedulerTest {

  @Mock
  private CaretakerRRuleRepository rruleRepository;

  @Mock
  private RRuleSlotGenerator slotGenerator;

  @Mock
  private SlotRepository slotRepository;

  @InjectMocks
  private SlotGenerationScheduler scheduler;

  private CaretakerRRule rrule1;
  private CaretakerRRule rrule2;
  private UUID rruleId1;
  private UUID rruleId2;

  @BeforeEach
  void setUp() {
    rruleId1 = UUID.randomUUID();
    rruleId2 = UUID.randomUUID();

    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());

    rrule1 = new CaretakerRRule();
    rrule1.setRruleId(rruleId1);
    rrule1.setCaretaker(caretaker);
    rrule1.setGeneratedTo(null); // Not yet generated

    rrule2 = new CaretakerRRule();
    rrule2.setRruleId(rruleId2);
    rrule2.setCaretaker(caretaker);
    rrule2.setGeneratedTo(LocalDate.now().plusDays(5)); // Already generated ahead
  }

  @Test
  public void testGenerateSlots_ProcessesRRulesNeedingGeneration() {
    LocalDate targetDate = LocalDate.now().plusDays(14);
    List<CaretakerRRule> rrules = Arrays.asList(rrule1);

    when(rruleRepository.findAllNeedingGeneration(any(), any())).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(10);

    SlotGenerationResult result = scheduler.generateSlots();

    assertEquals(1, result.rruleProcessed());
    assertEquals(10, result.slotsCreated());
    assertEquals(0, result.errors());
    verify(slotGenerator).generateSlotsForRRule(eq(rrule1), any(LocalDate.class), eq(targetDate));
  }

  @Test
  public void testGenerateSlots_SkipsAlreadyGeneratedRRules() {
    rrule1.setGeneratedTo(LocalDate.now().plusDays(20)); // Already ahead
    List<CaretakerRRule> rrules = Arrays.asList(rrule1);

    when(rruleRepository.findAllNeedingGeneration(any(), any())).thenReturn(rrules);

    SlotGenerationResult result = scheduler.generateSlots();

    assertEquals(1, result.rruleProcessed());
    assertEquals(0, result.slotsCreated());
    // Should not generate since already ahead of target
    verify(slotGenerator, never()).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  public void testGenerateSlots_StartsFromGeneratedToPlusOne() {
    rrule1.setGeneratedTo(LocalDate.now().plusDays(5));
    List<CaretakerRRule> rrules = Arrays.asList(rrule1);

    when(rruleRepository.findAllNeedingGeneration(any(), any())).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(15);

    scheduler.generateSlots();

    LocalDate expectedStartDate = LocalDate.now().plusDays(6); // generatedTo + 1
    verify(slotGenerator).generateSlotsForRRule(
        eq(rrule1),
        eq(expectedStartDate),
        any(LocalDate.class)
    );
  }

  @Test
  public void testGenerateSlots_HandlesErrors() {
    List<CaretakerRRule> rrules = Arrays.asList(rrule1, rrule2);

    when(rruleRepository.findAllNeedingGeneration(any(), any())).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(eq(rrule1), any(), any()))
        .thenThrow(new RuntimeException("Test error"));
    when(slotGenerator.generateSlotsForRRule(eq(rrule2), any(), any())).thenReturn(5);

    SlotGenerationResult result = scheduler.generateSlots();

    assertEquals(2, result.rruleProcessed());
    assertEquals(1, result.errors());
    assertTrue(result.slotsCreated() >= 0);
  }

  @Test
  public void testGenerateSlotsForSingleRRule_Success() {
    when(rruleRepository.findById(rruleId1)).thenReturn(Optional.of(rrule1));
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(20);

    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusDays(14);
    int result = scheduler.generateSlotsForSingleRRule(rruleId1, startDate, endDate);

    assertEquals(20, result);
    verify(slotGenerator).generateSlotsForRRule(rrule1, startDate, endDate);
  }

  @Test
  public void testGenerateSlotsForSingleRRule_NotFound() {
    when(rruleRepository.findById(rruleId1)).thenReturn(Optional.empty());

    int result = scheduler.generateSlotsForSingleRRule(rruleId1, LocalDate.now(), LocalDate.now().plusDays(14));

    assertEquals(0, result);
    verify(slotGenerator, never()).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  public void testDeleteSlotsForRRule_Success() {
    LocalDate fromDate = LocalDate.now();
    when(slotRepository.deleteByRRuleIdAndDateAfterAndUnoccupied(any(), any())).thenReturn(10);

    int result = scheduler.deleteSlotsForRRule(rruleId1, fromDate);

    assertEquals(10, result);
    verify(slotRepository).deleteByRRuleIdAndDateAfterAndUnoccupied(
        eq(rruleId1),
        eq(Date.valueOf(fromDate))
    );
  }

  @Test
  public void testDeleteSlotsForRRule_HandlesError() {
    when(slotRepository.deleteByRRuleIdAndDateAfterAndUnoccupied(any(), any()))
        .thenThrow(new RuntimeException("Test error"));

    int result = scheduler.deleteSlotsForRRule(rruleId1, LocalDate.now());

    assertEquals(0, result);
  }

  @Test
  public void testGenerateSlots_EmptyList() {
    when(rruleRepository.findAllNeedingGeneration(any(), any())).thenReturn(Collections.emptyList());

    SlotGenerationResult result = scheduler.generateSlots();

    assertEquals(0, result.rruleProcessed());
    assertEquals(0, result.slotsCreated());
    assertEquals(0, result.errors());
  }
}

