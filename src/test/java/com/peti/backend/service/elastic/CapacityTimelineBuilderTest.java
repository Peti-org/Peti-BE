package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CapacityTimelineBuilderTest {

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
  @DisplayName("Single RRule, no bookings - one segment spanning full range")
  void singleRRule_noBookings() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(), PREFS);

    assertThat(segments).hasSize(1);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalTime.of(20, 0));
    assertThat(segments.get(0).capacity()).isEqualTo(3);
    assertThat(segments.get(0).serviceConfig()).isEqualTo(WALKING_CONFIG);
  }

  @Test
  @DisplayName("Two overlapping RRules - three segments with different capacities")
  void overlappingRRules() {
    CaretakerRRule r1 = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 3);
    CaretakerRRule r2 = createRRule(LocalTime.of(18, 0), LocalTime.of(22, 0), 2);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(r1, r2), List.of(), PREFS);

    // 8-18 cap=3, 18-20 cap=5, 20-22 cap=2
    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).capacity()).isEqualTo(3);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalTime.of(18, 0));

    assertThat(segments.get(1).capacity()).isEqualTo(5);
    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalTime.of(18, 0));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalTime.of(20, 0));

    assertThat(segments.get(2).capacity()).isEqualTo(2);
    assertThat(segments.get(2).timeFrom()).isEqualTo(LocalTime.of(20, 0));
    assertThat(segments.get(2).timeTo()).isEqualTo(LocalTime.of(22, 0));
  }

  @Test
  @DisplayName("Booking splits timeline - zero capacity segment excluded")
  void bookingSplitsTimeline() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 2);
    BookingInput booking = new BookingInput(LocalTime.of(10, 0), LocalTime.of(14, 0), 2);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(booking), PREFS);

    // 8-10 cap=2, 10-14 cap=0 (excluded), 14-20 cap=2
    assertThat(segments).hasSize(2);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalTime.of(10, 0));
    assertThat(segments.get(0).capacity()).isEqualTo(2);

    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalTime.of(14, 0));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalTime.of(20, 0));
    assertThat(segments.get(1).capacity()).isEqualTo(2);
  }

  @Test
  @DisplayName("Partial booking reduces capacity without eliminating it")
  void partialBooking() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 5);
    BookingInput booking = new BookingInput(LocalTime.of(12, 0), LocalTime.of(16, 0), 3);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(booking), PREFS);

    // 8-12 cap=5, 12-16 cap=2, 16-20 cap=5
    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).capacity()).isEqualTo(5);
    assertThat(segments.get(1).capacity()).isEqualTo(2);
    assertThat(segments.get(2).capacity()).isEqualTo(5);
  }

  @Test
  @DisplayName("Empty rrules returns empty segments")
  void emptyRRules() {
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(), List.of(), PREFS);
    assertThat(segments).isEmpty();
  }

  @Test
  @DisplayName("Multiple bookings create multiple gaps")
  void multipleBookings() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), LocalTime.of(20, 0), 2);
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(9, 0), LocalTime.of(11, 0), 2),
        new BookingInput(LocalTime.of(15, 0), LocalTime.of(17, 0), 2)
    );
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), bookings, PREFS);

    // 8-9 cap=2, 9-11 cap=0(excl), 11-15 cap=2, 15-17 cap=0(excl), 17-20 cap=2
    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalTime.of(9, 0));
    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalTime.of(11, 0));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalTime.of(15, 0));
    assertThat(segments.get(2).timeFrom()).isEqualTo(LocalTime.of(17, 0));
    assertThat(segments.get(2).timeTo()).isEqualTo(LocalTime.of(20, 0));
  }

  private CaretakerRRule createRRule(LocalTime from, LocalTime to, int capacity) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.randomUUID());
    rrule.setDtstart(LocalDateTime.of(DATE, from));
    rrule.setDtend(LocalDateTime.of(DATE, to));
    rrule.setCapacity(capacity);
    rrule.setIsEnabled(true);
    rrule.setSlotType("WALKING");
    rrule.setPriority(0);
    return rrule;
  }
}

