package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.domain.User;
import com.peti.backend.repository.SlotRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for RRuleSlotGenerator service.
 */
class RRuleSlotGeneratorTest {

  private SlotRepository slotRepository;
  private EntityManager entityManager;
  private RRuleSlotGenerator slotGenerator;

  @BeforeEach
  void setUp() {
    slotRepository = mock(SlotRepository.class);
    entityManager = mock(EntityManager.class);
    slotGenerator = new RRuleSlotGenerator(slotRepository, entityManager);
  }

  @Test
  void testGenerateSlotsForRRule_WeeklyPattern_GeneratesCorrectSlots() {
    // Given: Weekly RRule for weekends (SA,SU)
    CaretakerRRule rrule = createTestRRule(
        "FREQ=WEEKLY;BYDAY=SA,SU",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 25);  // Saturday
    LocalDate endDate = LocalDate.of(2026, 2, 1);    // Next Sunday

    // Mock no existing slots
    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should create 3 slots (Jan 25 Sat, Jan 26 Sun, Feb 1 Sun)
    assertEquals(3, slotsCreated);
    verify(slotRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testGenerateSlotsForRRule_DailyPattern_GeneratesCorrectSlots() {
    // Given: Daily RRule
    CaretakerRRule rrule = createTestRRule(
        "FREQ=DAILY",
        LocalDateTime.of(2026, 1, 25, 10, 0),
        LocalDateTime.of(2026, 1, 31, 15, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 25);
    LocalDate endDate = LocalDate.of(2026, 1, 27);  // 3 days

    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should create 3 slots (25, 26, 27)
    assertEquals(3, slotsCreated);
  }

  @Test
  void testGenerateSlotsForRRule_SkipsExistingSlots() {
    // Given: Weekly RRule
    CaretakerRRule rrule = createTestRRule(
        "FREQ=WEEKLY;BYDAY=MO",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 17, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 26);  // Monday
    LocalDate endDate = LocalDate.of(2026, 2, 2);    // Next Monday

    // Mock: First slot exists, second doesn't
    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(true)   // Jan 26 exists
        .thenReturn(false); // Feb 2 doesn't exist
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should only create 1 slot (skips existing)
    assertEquals(1, slotsCreated);
  }

  @Test
  void testGenerateSlotsForRRule_RespectsDtendLimit() {
    // Given: RRule that ends on Jan 28
    CaretakerRRule rrule = createTestRRule(
        "FREQ=DAILY",
        LocalDateTime.of(2026, 1, 25, 9, 0),
        LocalDateTime.of(2026, 1, 28, 17, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 25);
    LocalDate endDate = LocalDate.of(2026, 1, 31);  // Request goes beyond dtend

    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should only create 4 slots (25, 26, 27, 28) - stops at dtend
    assertEquals(4, slotsCreated);
  }

  @Test
  void testGenerateSlotsForRRule_InvalidRRule_ReturnsZero() {
    // Given: Invalid RRule string
    CaretakerRRule rrule = createTestRRule(
        "INVALID_RRULE",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 17, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 25);
    LocalDate endDate = LocalDate.of(2026, 1, 31);

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should return 0 and not save anything
    assertEquals(0, slotsCreated);
    verify(slotRepository, never()).saveAll(anyList());
  }

  @Test
  void testGenerateSlotsForRRule_BatchSaving() {
    // Given: RRule that will create many slots (150+ days)
    CaretakerRRule rrule = createTestRRule(
        "FREQ=DAILY",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 17, 0)
    );

    LocalDate startDate = LocalDate.of(2026, 1, 1);
    LocalDate endDate = LocalDate.of(2026, 6, 1);  // ~150 days

    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    int slotsCreated = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Then: Should have called saveAll multiple times (batch size = 100)
    assertTrue(slotsCreated > 100);
    verify(slotRepository, times(2)).saveAll(anyList());  // At least 2 batches
  }

  // Helper method to create test RRule
  private CaretakerRRule createTestRRule(String rruleString, LocalDateTime dtstart, LocalDateTime dtend) {
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
    rrule.setRrule(rruleString);
    rrule.setDtstart(dtstart);
    rrule.setDtend(dtend);
    rrule.setSlotType("STANDARD");
    rrule.setDescription("Test RRule");
    rrule.setCreatedAt(LocalDateTime.now());

    return rrule;
  }
}

