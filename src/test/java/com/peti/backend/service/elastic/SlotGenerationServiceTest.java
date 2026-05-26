package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.BookingInput;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link SlotGenerationService}.
 */
class SlotGenerationServiceTest {

  private SlotGenerationService service;
  private Caretaker caretaker;
  private LocalDate testDate;
  private ServiceConfig defaultServiceConfig;

  @BeforeEach
  void setUp() {
    service = new SlotGenerationService();

    defaultServiceConfig = new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3,
        Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
        Map.of(), List.of("feeding")
    );

    CaretakerPreferences prefs = new CaretakerPreferences(
        List.of(defaultServiceConfig), null
    );

    City city = new City();
    city.setCityId(1L);
    city.setCity("Київ");

    User user = new User();
    user.setFirstName("Іван");
    user.setLastName("Шевченко");
    user.setCityByCityId(city);

    caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());
    caretaker.setRating(5);
    caretaker.setCaretakerPreference(prefs);
    caretaker.setUserReference(user);

    testDate = LocalDate.of(2026, 2, 15);
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private CaretakerRRule createRRule(LocalTime from, LocalTime to, int capacity) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.randomUUID());
    rrule.setSlotStartTime(from);
    rrule.setSlotDuration(Duration.between(from, to));
    rrule.setCapacity(capacity);
    rrule.setIsEnabled(true);
    rrule.setSlotType("WALKING");
    rrule.setPriority(0);
    return rrule;
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  @Test
  @DisplayName("No bookings - generates slots with capacity layers")
  void noBookings_singleSlotWithMaxCapacity() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3);

    List<ElasticSlotDocument> slots =
        service.generateSlotsForDay(testDate, List.of(rrule), List.of(), caretaker);

    // With no bookings, capacity levels 1,2,3 each get the full 8-20 range.
    assertThat(slots).hasSize(3);
    assertThat(slots).allMatch(s ->
        s.getFromDateTime().toLocalTime().equals(LocalTime.of(8, 0))
            && s.getToDateTime().toLocalTime().equals(LocalTime.of(20, 0)));
    assertThat(slots.stream().mapToInt(ElasticSlotDocument::getCapacity).max().orElse(0)).isEqualTo(3);
  }

  @Test
  @DisplayName("One booking in the middle - creates capacity-layered slots")
  void oneBookingInMiddle_optimizedSlots() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3);
    List<BookingInput> bookings = List.of(
        new BookingInput(testDate.atTime(10, 0), testDate.atTime(12, 0), 1)
    );

    List<ElasticSlotDocument> slots =
        service.generateSlotsForDay(testDate, List.of(rrule), bookings, caretaker);

    // Timeline: 8-10 cap=3, 10-12 cap=2, 12-20 cap=3
    // Capacity layers: cap1: 8-20, cap2: 8-20, cap3: 8-10 + 12-20
    assertThat(slots).hasSize(4);

    List<ElasticSlotDocument> cap3 = slots.stream().filter(s -> s.getCapacity() == 3).toList();
    assertThat(cap3).hasSize(2);
    assertThat(cap3).anyMatch(s ->
        s.getFromDateTime().toLocalTime().equals(LocalTime.of(8, 0))
            && s.getToDateTime().toLocalTime().equals(LocalTime.of(10, 0)));
    assertThat(cap3).anyMatch(s ->
        s.getFromDateTime().toLocalTime().equals(LocalTime.of(12, 0))
            && s.getToDateTime().toLocalTime().equals(LocalTime.of(20, 0)));

    List<ElasticSlotDocument> cap2 = slots.stream().filter(s -> s.getCapacity() == 2).toList();
    assertThat(cap2).hasSize(1);
    assertThat(cap2.get(0).getFromDateTime().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
    assertThat(cap2.get(0).getToDateTime().toLocalTime()).isEqualTo(LocalTime.of(20, 0));

    List<ElasticSlotDocument> cap1 = slots.stream().filter(s -> s.getCapacity() == 1).toList();
    assertThat(cap1).hasSize(1);
    assertThat(cap1.get(0).getFromDateTime().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
    assertThat(cap1.get(0).getToDateTime().toLocalTime()).isEqualTo(LocalTime.of(20, 0));
  }

  @Test
  @DisplayName("Full capacity booking - slots split around it")
  void fullCapacityBooking_slotsSplitAroundIt() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 2);
    List<BookingInput> bookings = List.of(
        new BookingInput(testDate.atTime(10, 0), testDate.atTime(12, 0), 2)
    );

    List<ElasticSlotDocument> slots =
        service.generateSlotsForDay(testDate, List.of(rrule), bookings, caretaker);

    assertThat(slots).isNotEmpty();
    assertThat(slots).allMatch(s ->
        !s.getFromDateTime().toLocalTime().isBefore(LocalTime.of(8, 0))
            && !s.getToDateTime().toLocalTime().isAfter(LocalTime.of(20, 0)));
    assertThat(slots.stream().mapToInt(ElasticSlotDocument::getCapacity).max().orElse(0)).isEqualTo(2);
    assertThat(slots).noneMatch(s -> s.getCapacity() > 2);
  }

  @Test
  @DisplayName("Null RRules returns empty list")
  void nullRRule_returnsEmptyList() {
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, null, List.of(), caretaker);
    assertThat(slots).isEmpty();
  }

  @Test
  @DisplayName("Zero capacity RRule returns empty list")
  void zeroCapacityRRule_returnsEmptyList() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 0);
    List<ElasticSlotDocument> slots =
        service.generateSlotsForDay(testDate, List.of(rrule), List.of(), caretaker);
    assertThat(slots).isEmpty();
  }

  @Test
  @DisplayName("Disabled RRule is ignored")
  void disabledRRule_isIgnored() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3);
    rrule.setIsEnabled(false);
    List<ElasticSlotDocument> slots =
        service.generateSlotsForDay(testDate, List.of(rrule), List.of(), caretaker);
    assertThat(slots).isEmpty();
  }
}
