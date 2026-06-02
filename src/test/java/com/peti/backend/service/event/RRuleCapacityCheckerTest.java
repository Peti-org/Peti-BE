package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.EventRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RRuleCapacityCheckerTest {

  private static final UUID CARETAKER_ID =
      UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");
  private static final LocalDateTime FROM = LocalDateTime.of(2026, 5, 2, 10, 0);
  private static final LocalDateTime TO = LocalDateTime.of(2026, 5, 2, 12, 0);

  @Mock private EventRepository eventRepository;
  @InjectMocks private RRuleCapacityChecker capacityChecker;

  @Test
  @DisplayName("Single rule fully covering range - sufficient capacity passes")
  void singleRule_sufficient() {
    // Rule covers 08:00-14:00, capacity 5. Event uses 2 pets in 10:00-12:00
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 5);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(eventWithPetsAndTime(2,
            LocalDateTime.of(2026, 5, 2, 10, 0),
            LocalDateTime.of(2026, 5, 2, 12, 0))));

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 3));
  }

  @Test
  @DisplayName("Single rule - insufficient capacity throws")
  void singleRule_insufficient() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 3);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(eventWithPetsAndTime(2,
            LocalDateTime.of(2026, 5, 2, 10, 0),
            LocalDateTime.of(2026, 5, 2, 12, 0))));

    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 2))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Insufficient capacity")
        .hasMessageContaining("Available: 1")
        .hasMessageContaining("requested: 2");
  }

  @Test
  @DisplayName("Two partially overlapping rules - bottleneck is the sub-interval with less capacity")
  void twoRules_partialOverlap_bottleneck() {
    // Request: 09:00 - 13:00
    // Rule A: 08:00-12:00, capacity 3 (covers 09:00-12:00)
    // Rule B: 10:00-14:00, capacity 2 (covers 10:00-13:00)
    // Sub-intervals: [09:00-10:00] -> only A = 3
    //                [10:00-12:00] -> A + B = 5
    //                [12:00-13:00] -> only B = 2  <-- bottleneck
    // Min capacity = 2
    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 9, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 13, 0);

    CaretakerRRule ruleA = buildRule(LocalTime.of(8, 0), Duration.ofHours(4), 3);
    CaretakerRRule ruleB = buildRule(LocalTime.of(10, 0), Duration.ofHours(4), 2);

    when(eventRepository.findActiveOverlapping(CARETAKER_ID, from, to))
        .thenReturn(List.of());

    // Available min = 2, requesting 2 should pass
    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 2));

    // Requesting 3 should fail (bottleneck at 12:00-13:00 has only 2)
    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 3))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Available: 2");
  }

  @Test
  @DisplayName("Events only overlap part of range - deducted only in that sub-interval")
  void eventPartialOverlap_deductedCorrectly() {
    // Request: 10:00-12:00
    // Rule: 08:00-14:00, capacity 3
    // Existing event: 11:00-12:00 with 2 pets
    // Sub-intervals: [10:00-11:00] -> cap 3, used 0 = 3
    //                [11:00-12:00] -> cap 3, used 2 = 1  <-- bottleneck
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 3);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(eventWithPetsAndTime(2,
            LocalDateTime.of(2026, 5, 2, 11, 0),
            LocalDateTime.of(2026, 5, 2, 12, 0))));

    // Available min = 1
    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 1));

    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 2))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Available: 1");
  }

  @Test
  @DisplayName("No existing events - full capacity available at minimum point")
  void noExistingEvents_fullCapacity() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 5);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of());

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 5));
  }

  @Test
  @DisplayName("Multiple events in same interval stack their pet counts")
  void multipleEvents_stackPetCounts() {
    // Rule: 08:00-14:00, capacity 5
    // Event 1: 10:00-12:00, 2 pets
    // Event 2: 10:00-11:00, 1 pet
    // Sub-intervals: [10:00-11:00] -> cap 5, used 3 = 2
    //               [11:00-12:00] -> cap 5, used 2 = 3
    // Min = 2
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 5);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(
            eventWithPetsAndTime(2, FROM, TO),
            eventWithPetsAndTime(1, FROM,
                LocalDateTime.of(2026, 5, 2, 11, 0))));

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 2));

    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 3))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Available: 2");
  }

  @Test
  @DisplayName("Adjacent rules with shared boundary - no gap, each covers its interval")
  void adjacentRules_sharedBoundary() {
    // Request: 08:00-12:00
    // Rule A: 08:00-10:00, capacity 2
    // Rule B: 10:00-12:00, capacity 4
    // Sub-intervals: [08:00-10:00] -> A = 2, [10:00-12:00] -> B = 4
    // Min capacity = 2
    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 8, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 12, 0);

    CaretakerRRule ruleA = buildRule(LocalTime.of(8, 0), Duration.ofHours(2), 2);
    CaretakerRRule ruleB = buildRule(LocalTime.of(10, 0), Duration.ofHours(2), 4);

    when(eventRepository.findActiveOverlapping(CARETAKER_ID, from, to))
        .thenReturn(List.of());

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 2));

    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 3))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Available: 2");
  }

  @Test
  @DisplayName("Adjacent rules with event exactly at boundary point")
  void adjacentRules_eventAtBoundary() {
    // Request: 08:00-12:00
    // Rule A: 08:00-10:00, capacity 3
    // Rule B: 10:00-12:00, capacity 3
    // Event: 09:00-11:00 with 2 pets (spans boundary)
    // Sub-intervals: [08:00-09:00] -> A=3, used 0 = 3
    //               [09:00-10:00] -> A=3, used 2 = 1
    //               [10:00-11:00] -> B=3, used 2 = 1
    //               [11:00-12:00] -> B=3, used 0 = 3
    // Min = 1
    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 8, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 12, 0);

    CaretakerRRule ruleA = buildRule(LocalTime.of(8, 0), Duration.ofHours(2), 3);
    CaretakerRRule ruleB = buildRule(LocalTime.of(10, 0), Duration.ofHours(2), 3);

    when(eventRepository.findActiveOverlapping(CARETAKER_ID, from, to))
        .thenReturn(List.of(eventWithPetsAndTime(2,
            LocalDateTime.of(2026, 5, 2, 9, 0),
            LocalDateTime.of(2026, 5, 2, 11, 0))));

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 1));

    assertThatThrownBy(() ->
        capacityChecker.validateCapacity(List.of(ruleA, ruleB), CARETAKER_ID, from, to, 2))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Available: 1");
  }

  @Test
  @DisplayName("Event with null pets treated as zero usage")
  void eventWithNullPets_treatedAsZero() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 3);
    Event event = new Event();
    event.setDatetimeFrom(FROM);
    event.setDatetimeTo(TO);
    event.setPets(null);

    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(event));

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 3));
  }

  @Test
  @DisplayName("Event with empty pets set treated as zero usage")
  void eventWithEmptyPets_treatedAsZero() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 3);
    Event event = new Event();
    event.setDatetimeFrom(FROM);
    event.setDatetimeTo(TO);
    event.setPets(new HashSet<>());

    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(event));

    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 3));
  }

  @Test
  @DisplayName("Requesting zero pets always passes")
  void requestingZeroPets_alwaysPasses() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(6), 1);
    when(eventRepository.findActiveOverlapping(CARETAKER_ID, FROM, TO))
        .thenReturn(List.of(eventWithPetsAndTime(1, FROM, TO)));

    // Even though capacity is fully used, requesting 0 should pass
    assertThatNoException().isThrownBy(() ->
        capacityChecker.validateCapacity(List.of(rule), CARETAKER_ID, FROM, TO, 0));
  }

  private CaretakerRRule buildRule(LocalTime start, Duration duration, int capacity) {
    CaretakerRRule rule = new CaretakerRRule();
    rule.setRruleId(UUID.randomUUID());
    rule.setSlotStartTime(start);
    rule.setSlotDuration(duration);
    rule.setCapacity(capacity);
    rule.setIsEnabled(true);
    return rule;
  }

  private Event eventWithPetsAndTime(int petCount,
      LocalDateTime from, LocalDateTime to) {
    Event event = new Event();
    event.setDatetimeFrom(from);
    event.setDatetimeTo(to);
    Set<Pet> pets = new HashSet<>();
    for (int i = 0; i < petCount; i++) {
      Pet pet = new Pet();
      pet.setPetId(UUID.randomUUID());
      pets.add(pet);
    }
    event.setPets(pets);
    return event;
  }
}
