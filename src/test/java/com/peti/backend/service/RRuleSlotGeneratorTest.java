package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Slot;
import com.peti.backend.model.internal.TimeSlotPair;
import com.peti.backend.repository.SlotRepository;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RRuleSlotGeneratorTest {

  @Mock
  private SlotRepository slotRepository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private SlotDivider slotDivider;

  @InjectMocks
  private RRuleSlotGenerator slotGenerator;

  private CaretakerRRule rrule;
  private Caretaker caretaker;
  private UUID caretakerId;

  @BeforeEach
  void setUp() {
    caretakerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");

    caretaker = new Caretaker();
    caretaker.setCaretakerId(caretakerId);

    rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));
    rrule.setCaretaker(caretaker);
    rrule.setRrule("FREQ=DAILY;COUNT=3");
    rrule.setDtstart(LocalDateTime.of(2026, 2, 10, 9, 0));
    rrule.setDtend(LocalDateTime.of(2026, 2, 10, 17, 0));
    rrule.setSlotType("walk");
    rrule.setCapacity(5);
    rrule.setIntervalMinutes(30);
  }

  @Test
  public void testGenerateSlotsForRRule_UsesSlotDivider() {
    LocalDate startDate = LocalDate.of(2026, 2, 10);
    LocalDate endDate = LocalDate.of(2026, 2, 12);

    // Mock SlotDivider to return 16 time slots per day (8 hours / 30 min)
    List<TimeSlotPair> timeSlots = Arrays.asList(
        new TimeSlotPair(LocalTime.of(9, 0), LocalTime.of(9, 30)),
        new TimeSlotPair(LocalTime.of(9, 30), LocalTime.of(10, 0)),
        new TimeSlotPair(LocalTime.of(10, 0), LocalTime.of(10, 30)),
        new TimeSlotPair(LocalTime.of(10, 30), LocalTime.of(11, 0))
    );
    when(slotDivider.divideTimeRange(any(LocalTime.class), any(LocalTime.class), any(Integer.class)))
        .thenReturn(timeSlots);
    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    when(entityManager.merge(any())).thenReturn(rrule);

    int result = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Should generate slots for 3 days (COUNT=3), 1 time slot per day = 3 total
    assertEquals(4, result);
    verify(slotRepository).saveAll(anyList());
  }

  @Test
  public void testGenerateSlotsForRRule_SkipsExistingSlots() {
    LocalDate startDate = LocalDate.of(2026, 2, 10);
    LocalDate endDate = LocalDate.of(2026, 2, 10);

    List<TimeSlotPair> timeSlots = Arrays.asList(
        new TimeSlotPair(LocalTime.of(9, 0), LocalTime.of(9, 30)),
        new TimeSlotPair(LocalTime.of(9, 30), LocalTime.of(10, 0))
    );
    when(slotDivider.divideTimeRange(any(), any(), anyInt())).thenReturn(timeSlots);

    // First slot exists, second doesn't
    when(slotRepository.existsByCaretakerIdAndDateAndTime(
        eq(caretakerId),
        eq(Date.valueOf(startDate)),
        eq(Time.valueOf(LocalTime.of(9, 0))),
        eq(Time.valueOf(LocalTime.of(9, 30)))
    )).thenReturn(true);

    when(slotRepository.existsByCaretakerIdAndDateAndTime(
        eq(caretakerId),
        eq(Date.valueOf(startDate)),
        eq(Time.valueOf(LocalTime.of(9, 30))),
        eq(Time.valueOf(LocalTime.of(10, 0)))
    )).thenReturn(false);

    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    when(entityManager.merge(any())).thenReturn(rrule);

    int result = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    // Should only create 1 slot (the non-existing one)
    assertEquals(1, result);
  }

  @Test
  public void testGenerateSlotsForRRule_SetsIsRepeatedTrue() {
    LocalDate startDate = LocalDate.of(2026, 2, 10);
    LocalDate endDate = LocalDate.of(2026, 2, 10);

    List<TimeSlotPair> timeSlots = Arrays.asList(
        new TimeSlotPair(LocalTime.of(9, 0), LocalTime.of(9, 30))
    );
    when(slotDivider.divideTimeRange(any(), any(), anyInt())).thenReturn(timeSlots);
    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(entityManager.merge(any())).thenReturn(rrule);

    // Capture saved slots
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> {
      List<Slot> slots = invocation.getArgument(0);
      for (Slot slot : slots) {
        assertTrue(slot.getIsRepeated(), "Slot should have isRepeated = true");
        assertEquals(rrule, slot.getRrule(), "Slot should reference the RRule");
        assertEquals(5, slot.getCapacity(), "Slot should use RRule capacity");
      }
      return slots;
    });

    slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);
  }

  @Test
  public void testGenerateSlotsForRRule_UpdatesGeneratedTo() {
    LocalDate startDate = LocalDate.of(2026, 2, 10);
    LocalDate endDate = LocalDate.of(2026, 2, 12);

    List<TimeSlotPair> timeSlots = Arrays.asList(
        new TimeSlotPair(LocalTime.of(9, 0), LocalTime.of(9, 30))
    );
    when(slotDivider.divideTimeRange(any(), any(), anyInt())).thenReturn(timeSlots);
    when(slotRepository.existsByCaretakerIdAndDateAndTime(any(), any(), any(), any()))
        .thenReturn(false);
    when(slotRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
    when(entityManager.merge(any())).thenReturn(rrule);

    slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    verify(entityManager).merge(any(CaretakerRRule.class));
    // The RRule's generatedTo should be updated to the last generated date
  }

  @Test
  public void testGenerateSlotsForRRule_InvalidRRule() {
    rrule.setRrule("INVALID_RRULE");
    LocalDate startDate = LocalDate.of(2026, 2, 10);
    LocalDate endDate = LocalDate.of(2026, 2, 12);

    int result = slotGenerator.generateSlotsForRRule(rrule, startDate, endDate);

    assertEquals(0, result);
  }
}

