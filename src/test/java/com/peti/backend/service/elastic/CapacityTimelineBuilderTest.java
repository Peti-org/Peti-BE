package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import com.peti.backend.service.elastic.builder.CapacityTimelineBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CapacityTimelineBuilderTest {

  private static final LocalDate DATE = LocalDate.of(2026, 3, 1);

  @Test
  @DisplayName("Single RRule, no bookings - one segment spanning full range")
  void singleRRule_noBookings() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 3);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(), DATE);

    assertThat(segments).hasSize(1);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(8, 0)));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(20, 0)));
    assertThat(segments.get(0).capacity()).isEqualTo(3);
  }

  @Test
  @DisplayName("Two overlapping RRules - three segments with different capacities")
  void overlappingRRules() {
    CaretakerRRule r1 = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 3);
    CaretakerRRule r2 = createRRule(LocalTime.of(18, 0), Duration.ofHours(4), 2);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(r1, r2), List.of(), DATE);

    // 8-18 cap=3, 18-20 cap=5, 20-22 cap=2
    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).capacity()).isEqualTo(3);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(8, 0)));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(18, 0)));

    assertThat(segments.get(1).capacity()).isEqualTo(5);
    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(18, 0)));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(20, 0)));

    assertThat(segments.get(2).capacity()).isEqualTo(2);
    assertThat(segments.get(2).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(20, 0)));
    assertThat(segments.get(2).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(22, 0)));
  }

  @Test
  @DisplayName("Booking splits timeline - zero capacity segment excluded")
  void bookingSplitsTimeline() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 2);
    BookingInput booking = new BookingInput(
        LocalDateTime.of(DATE, LocalTime.of(10, 0)),
        LocalDateTime.of(DATE, LocalTime.of(14, 0)), 2);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(booking), DATE);

    assertThat(segments).hasSize(2);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(8, 0)));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(10, 0)));
    assertThat(segments.get(0).capacity()).isEqualTo(2);

    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(14, 0)));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(20, 0)));
    assertThat(segments.get(1).capacity()).isEqualTo(2);
  }

  @Test
  @DisplayName("Partial booking reduces capacity without eliminating it")
  void partialBooking() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 5);
    BookingInput booking = new BookingInput(
        LocalDateTime.of(DATE, LocalTime.of(12, 0)),
        LocalDateTime.of(DATE, LocalTime.of(16, 0)), 3);
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(booking), DATE);

    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).capacity()).isEqualTo(5);
    assertThat(segments.get(1).capacity()).isEqualTo(2);
    assertThat(segments.get(2).capacity()).isEqualTo(5);
  }

  @Test
  @DisplayName("Empty rrules returns empty segments")
  void emptyRRules() {
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(), List.of(), DATE);
    assertThat(segments).isEmpty();
  }

  @Test
  @DisplayName("Multiple bookings create multiple gaps")
  void multipleBookings() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 2);
    List<BookingInput> bookings = List.of(
        new BookingInput(
            LocalDateTime.of(DATE, LocalTime.of(9, 0)),
            LocalDateTime.of(DATE, LocalTime.of(11, 0)), 2),
        new BookingInput(
            LocalDateTime.of(DATE, LocalTime.of(15, 0)),
            LocalDateTime.of(DATE, LocalTime.of(17, 0)), 2)
    );
    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), bookings, DATE);

    assertThat(segments).hasSize(3);
    assertThat(segments.get(0).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(8, 0)));
    assertThat(segments.get(0).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(9, 0)));
    assertThat(segments.get(1).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(11, 0)));
    assertThat(segments.get(1).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(15, 0)));
    assertThat(segments.get(2).timeFrom()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(17, 0)));
    assertThat(segments.get(2).timeTo()).isEqualTo(LocalDateTime.of(DATE, LocalTime.of(20, 0)));
  }

  @Test
  @DisplayName("RRule with FREQ=WEEKLY;BYDAY=MO is not active on a Sunday")
  void rruleNotActiveOnDate() {
    CaretakerRRule rrule = createRRule(LocalTime.of(8, 0), Duration.ofHours(12), 3);
    rrule.setRrule("FREQ=WEEKLY;BYDAY=MO");

    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(), DATE);

    assertThat(segments).isEmpty();
  }

  @Test
  @DisplayName("RRule with FREQ=DAILY is active on the given date")
  void rruleDailyActiveOnDate() {
    CaretakerRRule rrule = createRRule(LocalTime.of(9, 0), Duration.ofHours(8), 2);
    rrule.setRrule("FREQ=DAILY");

    List<TimeSegmentWithPricing> segments =
        CapacityTimelineBuilder.buildSegments(List.of(rrule), List.of(), DATE);

    assertThat(segments).hasSize(1);
    assertThat(segments.get(0).capacity()).isEqualTo(2);
  }

  private CaretakerRRule createRRule(LocalTime startTime, Duration duration, int capacity) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setRruleId(UUID.randomUUID());
    rrule.setSlotStartTime(startTime);
    rrule.setSlotDuration(duration);
    rrule.setCapacity(capacity);
    rrule.setIsEnabled(true);
    rrule.setSlotType("WALKING");
    rrule.setPriority(0);
    return rrule;
  }
}
