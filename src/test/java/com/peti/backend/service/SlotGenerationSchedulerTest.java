package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.service.SlotGenerationScheduler.SlotGenerationResult;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for SlotGenerationScheduler service.
 */
class SlotGenerationSchedulerTest {

  private CaretakerRRuleRepository rruleRepository;
  private RRuleSlotGenerator slotGenerator;
  private SlotGenerationScheduler scheduler;

  @BeforeEach
  void setUp() {
    rruleRepository = mock(CaretakerRRuleRepository.class);
    slotGenerator = mock(RRuleSlotGenerator.class);
    scheduler = new SlotGenerationScheduler(rruleRepository, slotGenerator);

    // Set default configuration values
    ReflectionTestUtils.setField(scheduler, "daysAhead", 14);
    ReflectionTestUtils.setField(scheduler, "batchSize", 50);
  }

  @Test
  void testGenerateSlots_NoActiveRRules_ReturnsZero() {
    // Given: No active RRules
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(List.of());

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then
    assertNotNull(result);
    assertEquals(0, result.rruleProcessed());
    assertEquals(0, result.slotsCreated());
    assertEquals(0, result.errors());
  }

  @Test
  void testGenerateSlots_SingleRRule_GeneratesSlots() {
    // Given: One active RRule
    List<CaretakerRRule> rrules = List.of(createTestRRule());
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(10);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then
    assertEquals(1, result.rruleProcessed());
    assertEquals(10, result.slotsCreated());
    assertEquals(0, result.errors());
    verify(slotGenerator, times(1)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateSlots_MultipleRRules_GeneratesAllSlots() {
    // Given: Multiple active RRules
    List<CaretakerRRule> rrules = List.of(
        createTestRRule(),
        createTestRRule(),
        createTestRRule()
    );
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any()))
        .thenReturn(5)
        .thenReturn(10)
        .thenReturn(7);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then
    assertEquals(3, result.rruleProcessed());
    assertEquals(22, result.slotsCreated());  // 5 + 10 + 7
    assertEquals(0, result.errors());
    verify(slotGenerator, times(3)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateSlots_WithErrors_ContinuesProcessing() {
    // Given: Multiple RRules, one throws exception
    List<CaretakerRRule> rrules = List.of(
        createTestRRule(),
        createTestRRule(),
        createTestRRule()
    );
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any()))
        .thenReturn(5)
        .thenThrow(new RuntimeException("Test error"))
        .thenReturn(7);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then: Should continue processing despite error
    assertEquals(2, result.rruleProcessed());  // Only 2 succeeded
    assertEquals(12, result.slotsCreated());   // 5 + 7
    assertEquals(1, result.errors());
    verify(slotGenerator, times(3)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateSlots_LargeNumberOfRRules_ProcessesInBatches() {
    // Given: 150 RRules (batch size is 50)
    List<CaretakerRRule> rrules = new ArrayList<>();
    for (int i = 0; i < 150; i++) {
      rrules.add(createTestRRule());
    }
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(5);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then: All 150 RRules should be processed
    assertEquals(150, result.rruleProcessed());
    assertEquals(750, result.slotsCreated());  // 150 * 5
    assertEquals(0, result.errors());
    verify(slotGenerator, times(150)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateDailySlots_CallsGenerateSlots() {
    // Given: One active RRule
    when(rruleRepository.findAllActive(any(LocalDateTime.class)))
        .thenReturn(List.of(createTestRRule()));
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(5);

    // When: Scheduled method is called
    scheduler.generateDailySlots();

    // Then: Should call generateSlots internally
    verify(rruleRepository, times(1)).findAllActive(any(LocalDateTime.class));
    verify(slotGenerator, times(1)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateSlots_CustomBatchSize() {
    // Given: 75 RRules with batch size 25
    ReflectionTestUtils.setField(scheduler, "batchSize", 25);

    List<CaretakerRRule> rrules = new ArrayList<>();
    for (int i = 0; i < 75; i++) {
      rrules.add(createTestRRule());
    }
    when(rruleRepository.findAllActive(any(LocalDateTime.class))).thenReturn(rrules);
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(3);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then: All 75 should be processed in 3 batches (25 + 25 + 25)
    assertEquals(75, result.rruleProcessed());
    assertEquals(225, result.slotsCreated());  // 75 * 3
    verify(slotGenerator, times(75)).generateSlotsForRRule(any(), any(), any());
  }

  @Test
  void testGenerateSlots_CustomDaysAhead() {
    // Given: Custom days ahead = 30
    ReflectionTestUtils.setField(scheduler, "daysAhead", 30);

    when(rruleRepository.findAllActive(any(LocalDateTime.class)))
        .thenReturn(List.of(createTestRRule()));
    when(slotGenerator.generateSlotsForRRule(any(), any(), any())).thenReturn(20);

    // When
    SlotGenerationResult result = scheduler.generateSlots();

    // Then
    assertEquals(1, result.rruleProcessed());
    assertEquals(20, result.slotsCreated());
    // Verify that endDate calculation uses 30 days ahead
    verify(slotGenerator, times(1)).generateSlotsForRRule(any(), any(), any());
  }

  // Helper method to create test RRule
  private CaretakerRRule createTestRRule() {
    User user = new User();
    user.setUserId(UUID.randomUUID());
    user.setFirstName("Test");
    user.setLastName("User");

    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());
    caretaker.setUserReference(user);

    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.randomUUID());
    rrule.setCaretaker(caretaker);
    rrule.setRrule("FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR");
    rrule.setDtstart(LocalDateTime.now());
    rrule.setDtend(LocalDateTime.now().plusMonths(6));
    rrule.setSlotType("STANDARD");
    rrule.setDescription("Test RRule");
    rrule.setCreatedAt(LocalDateTime.now());

    return rrule;
  }
}

