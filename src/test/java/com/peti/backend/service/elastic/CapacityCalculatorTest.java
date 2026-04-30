package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.CapacityWithPricing;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CapacityCalculatorTest {

  private static final LocalDate DATE = LocalDate.of(2026, 3, 1);
  private static final ServiceConfig WALKING_CONFIG = new ServiceConfig(
      ServiceType.WALKING, false, true, false, 3,
      Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
      Map.of(), List.of()
  );
  private static final CaretakerPreferences PREFS = new CaretakerPreferences(
      List.of(WALKING_CONFIG), Map.of()
  );

  @Test
  @DisplayName("Single RRule, no bookings - returns full capacity")
  void singleRRule_noBookings() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 5, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(rrule), List.of(), PREFS);
    assertThat(result.capacity()).isEqualTo(5);
    assertThat(result.serviceConfig()).isEqualTo(WALKING_CONFIG);
  }

  @Test
  @DisplayName("Time outside RRule range - returns zero capacity")
  void timeOutsideRange() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(12, 0), 3, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(14, 0), List.of(rrule), List.of(), PREFS);
    assertThat(result.capacity()).isZero();
    assertThat(result.serviceConfig()).isNull();
  }

  @Test
  @DisplayName("Two overlapping RRules - capacities are summed")
  void overlappingRRules_capacitiesSummed() {
    CaretakerRRule r1 = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3, 0);
    CaretakerRRule r2 = createRRule(LocalTime.of(10, 0), LocalTime.of(14, 0), 2, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(r1, r2), List.of(), PREFS);
    assertThat(result.capacity()).isEqualTo(5);
  }

  @Test
  @DisplayName("Booking reduces capacity")
  void bookingReducesCapacity() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 5, 0);
    BookingInput booking = new BookingInput(LocalTime.of(10, 0), LocalTime.of(14, 0), 3);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(rrule), List.of(booking), PREFS);
    assertThat(result.capacity()).isEqualTo(2);
  }

  @Test
  @DisplayName("Booking exceeding capacity - returns zero (clamped)")
  void bookingExceedingCapacity_clampedToZero() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 2, 0);
    BookingInput booking = new BookingInput(LocalTime.of(10, 0), LocalTime.of(14, 0), 5);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(rrule), List.of(booking), PREFS);
    assertThat(result.capacity()).isZero();
  }

  @Test
  @DisplayName("Higher priority RRule wins service config")
  void higherPriorityWins() {
    ServiceConfig sittingConfig = new ServiceConfig(
        ServiceType.SITTING, false, true, false, 1,
        Duration.ofMinutes(60), Duration.ofMinutes(30), Duration.ofHours(1),
        Map.of(), List.of()
    );
    CaretakerPreferences prefs = new CaretakerPreferences(
        List.of(WALKING_CONFIG, sittingConfig), Map.of()
    );
    CaretakerRRule r1 = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3, 0);
    r1.setSlotType("WALKING");
    CaretakerRRule r2 = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 2, 5);
    r2.setSlotType("SITTING");

    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(r1, r2), List.of(), prefs);
    assertThat(result.capacity()).isEqualTo(5);
    assertThat(result.serviceConfig().type()).isEqualTo(ServiceType.SITTING);
  }

  @Test
  @DisplayName("Null preferences - returns null service config")
  void nullPreferences() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(12, 0), List.of(rrule), List.of(), null);
    assertThat(result.capacity()).isEqualTo(3);
    assertThat(result.serviceConfig()).isNull();
  }

  @Test
  @DisplayName("Time at exact start boundary is included")
  void timeAtStartBoundary() {
    CaretakerRRule rrule = createRRule(LocalTime.of(10, 0), LocalTime.of(14, 0), 3, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(10, 0), List.of(rrule), List.of(), PREFS);
    assertThat(result.capacity()).isEqualTo(3);
  }

  @Test
  @DisplayName("Time at exact end boundary is excluded")
  void timeAtEndBoundary() {
    CaretakerRRule rrule = createRRule(LocalTime.of(10, 0), LocalTime.of(14, 0), 3, 0);
    CapacityWithPricing result = CapacityCalculator.computeAt(
        LocalTime.of(14, 0), List.of(rrule), List.of(), PREFS);
    assertThat(result.capacity()).isZero();
  }

  private CaretakerRRule createRRule(LocalTime from, LocalTime to, int capacity, int priority) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.randomUUID());
    rrule.setDtstart(LocalDateTime.of(DATE, from));
    rrule.setDtend(LocalDateTime.of(DATE, to));
    rrule.setCapacity(capacity);
    rrule.setIsEnabled(true);
    rrule.setSlotType("WALKING");
    rrule.setPriority(priority);
    return rrule;
  }
}

