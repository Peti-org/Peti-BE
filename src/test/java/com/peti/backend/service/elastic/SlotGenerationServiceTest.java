package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.PricingConfig;
import com.peti.backend.service.elastic.SlotGenerationService.BookingInput;
import com.peti.backend.service.elastic.SlotGenerationService.CaretakerInput;
import com.peti.backend.service.elastic.SlotGenerationService.RRuleInput;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotGenerationServiceTest {

  private SlotGenerationService service;
  private CaretakerInput caretaker;
  private LocalDate testDate;
  private PricingConfig defaultPricingConfig;

  @BeforeEach
  void setUp() {
    service = new SlotGenerationService();
    caretaker = new CaretakerInput(
        "caretaker-1",
        "Іван",
        "Шевченко",
        5,
        "city-1",
        "Київ",
        null
    );
    testDate = LocalDate.of(2026, 2, 15);
    
    // Default pricing config for tests
    defaultPricingConfig = PricingConfig.builder()
        .minDurationMinutes(60)
        .stepMinutes(15)
        .basePricePerMinDuration(BigDecimal.valueOf(100))
        .pricePerStep(BigDecimal.valueOf(30))
        .currency("UAH")
        .build();
  }

  @Test
  @DisplayName("No bookings - single slot with max capacity")
  void noBookings_singleSlotWithMaxCapacity() {
    // Given: RRule 8-20, capacity 3, no bookings
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        3,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of();

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Then: only 1 slot with max capacity (not 3 slots for each level)
    assertThat(slots).hasSize(1);
    
    ElasticSlotDocument slot = slots.get(0);
    assertThat(slot.getTimeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(slot.getTimeTo()).isEqualTo(LocalTime.of(20, 0));
    assertThat(slot.getCapacity()).isEqualTo(3);
  }

  @Test
  @DisplayName("One booking in the middle - optimized slots")
  void oneBookingInMiddle_optimizedSlots() {
    // Given: RRule 8-20, capacity 3, booking 10-12 for 1 pet
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        3,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(10, 0), LocalTime.of(12, 0), 1)
    );

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Then:
    // Timeline: 8-10 cap=3, 10-12 cap=2, 12-20 cap=3
    // Cap 3: 8-10, 12-20 (2 unique ranges)
    // Cap 2: 8-20 (but 8-10 and 12-20 already covered by cap 3, so only 8-20 is truly new if continuous)
    // Actually cap 2 is continuous 8-20 (all segments have cap >= 2)
    // But 8-10 already has cap 3, 12-20 already has cap 3
    // So we need: 8-10 cap 3, 12-20 cap 3, and 8-20 cap 2 (unique range)
    // Total: 3 slots
    assertThat(slots).hasSize(3);

    // Cap 3 slots (8-10 and 12-20)
    List<ElasticSlotDocument> cap3Slots = slots.stream().filter(s -> s.getCapacity() == 3).toList();
    assertThat(cap3Slots).hasSize(2);
    assertThat(cap3Slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && s.getTimeTo().equals(LocalTime.of(10, 0)));
    assertThat(cap3Slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(12, 0)) && s.getTimeTo().equals(LocalTime.of(20, 0)));

    // Cap 2 slot - 8-20 (continuous range different from cap 3 ranges)
    List<ElasticSlotDocument> cap2Slots = slots.stream().filter(s -> s.getCapacity() == 2).toList();
    assertThat(cap2Slots).hasSize(1);
    assertThat(cap2Slots.get(0).getTimeFrom()).isEqualTo(LocalTime.of(8, 0));
    assertThat(cap2Slots.get(0).getTimeTo()).isEqualTo(LocalTime.of(20, 0));
  }

  @Test
  @DisplayName("Two overlapping bookings - optimized slot structure")
  void twoOverlappingBookings_optimizedSlotStructure() {
    // Given: RRule 8-20, capacity 3
    // Booking 1: 10-12 for 1 pet
    // Booking 2: 11-14 for 2 pets
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        3,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(10, 0), LocalTime.of(12, 0), 1),
        new BookingInput(LocalTime.of(11, 0), LocalTime.of(14, 0), 2)
    );

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Timeline:
    // 8-10: cap=3 (no bookings)
    // 10-11: cap=2 (booking 1 active: 3-1=2)
    // 11-12: cap=0 (both bookings: 3-1-2=0)
    // 12-14: cap=1 (booking 2 active: 3-2=1)
    // 14-20: cap=3 (no bookings)
    
    // Unique ranges per capacity level (highest capacity wins for same range):
    // Cap 3: 8-10, 14-20
    // Cap 2: 8-11 (unique, different from 8-10)
    // Cap 1: 8-11 SAME as cap 2 range - skip (cap 2 is higher)
    //        12-20 (unique, different from 14-20)
    // 
    // Final: 8-10 cap3, 14-20 cap3, 8-11 cap2, 12-20 cap1
    // Total: 4 slots
    assertThat(slots).hasSize(4);

    // Verify specific slots exist
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && 
        s.getTimeTo().equals(LocalTime.of(10, 0)) && 
        s.getCapacity() == 3);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(14, 0)) && 
        s.getTimeTo().equals(LocalTime.of(20, 0)) && 
        s.getCapacity() == 3);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && 
        s.getTimeTo().equals(LocalTime.of(11, 0)) && 
        s.getCapacity() == 2);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(12, 0)) && 
        s.getTimeTo().equals(LocalTime.of(20, 0)) && 
        s.getCapacity() == 1);
  }

  @Test
  @DisplayName("Full capacity booking - slots split around it")
  void fullCapacityBooking_slotsSplitAroundIt() {
    // Given: RRule 8-20, capacity 2, booking 10-12 for 2 pets (full capacity)
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        2,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(10, 0), LocalTime.of(12, 0), 2)
    );

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Timeline: 8-10 cap=2, 10-12 cap=0, 12-20 cap=2
    // Cap 2: 8-10, 12-20 (2 ranges, no continuous 8-20 because of gap)
    // Cap 1: 8-10, 12-20 (same ranges as cap 2 - SKIP, cap 2 already covers them)
    // Total: 2 slots
    assertThat(slots).hasSize(2);

    // Both should have capacity 2
    assertThat(slots).allMatch(s -> s.getCapacity() == 2);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && s.getTimeTo().equals(LocalTime.of(10, 0)));
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(12, 0)) && s.getTimeTo().equals(LocalTime.of(20, 0)));
  }

  @Test
  @DisplayName("Booking at start of day - optimized slots")
  void bookingAtStartOfDay_optimizedSlots() {
    // Given: RRule 8-20, capacity 3, booking 8-10 for 1 pet
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        3,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(8, 0), LocalTime.of(10, 0), 1)
    );

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Timeline: 8-10 cap=2, 10-20 cap=3
    // Cap 3: 10-20 (one range)
    // Cap 2: 8-20 (continuous, different from 10-20)
    // Cap 1: 8-20 (same as cap 2 - SKIP)
    // Total: 2 slots
    assertThat(slots).hasSize(2);

    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(10, 0)) && 
        s.getTimeTo().equals(LocalTime.of(20, 0)) && 
        s.getCapacity() == 3);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && 
        s.getTimeTo().equals(LocalTime.of(20, 0)) && 
        s.getCapacity() == 2);
  }

  @Test
  @DisplayName("Booking at end of day - optimized slots")
  void bookingAtEndOfDay_optimizedSlots() {
    // Given: RRule 8-20, capacity 3, booking 18-20 for 1 pet
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        3,
        defaultPricingConfig
    );
    List<BookingInput> bookings = List.of(
        new BookingInput(LocalTime.of(18, 0), LocalTime.of(20, 0), 1)
    );

    // When
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, bookings, caretaker);

    // Timeline: 8-18 cap=3, 18-20 cap=2
    // Cap 3: 8-18 (one range)
    // Cap 2: 8-20 (continuous, different from 8-18)
    // Cap 1: 8-20 (same as cap 2 - SKIP)
    // Total: 2 slots
    assertThat(slots).hasSize(2);

    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && 
        s.getTimeTo().equals(LocalTime.of(18, 0)) && 
        s.getCapacity() == 3);
    assertThat(slots).anyMatch(s -> 
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && 
        s.getTimeTo().equals(LocalTime.of(20, 0)) && 
        s.getCapacity() == 2);
  }

  @Test
  @DisplayName("Null RRule returns empty list")
  void nullRRule_returnsEmptyList() {
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, null, List.of(), caretaker);
    assertThat(slots).isEmpty();
  }

  @Test
  @DisplayName("Zero capacity RRule returns empty list")
  void zeroCapacityRRule_returnsEmptyList() {
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        0,
        defaultPricingConfig
    );
    
    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, List.of(), caretaker);
    assertThat(slots).isEmpty();
  }

  @Test
  @DisplayName("Slots have correct caretaker info")
  void slotsHaveCorrectCaretakerInfo() {
    PricingConfig pricingConfig = PricingConfig.builder()
        .minDurationMinutes(60)
        .stepMinutes(15)
        .basePricePerMinDuration(BigDecimal.valueOf(200))
        .pricePerStep(BigDecimal.valueOf(50))
        .currency("UAH")
        .build();
        
    RRuleInput rrule = new RRuleInput(
        LocalTime.of(8, 0),
        LocalTime.of(20, 0),
        1,
        pricingConfig
    );

    List<ElasticSlotDocument> slots = service.generateSlotsForDay(testDate, rrule, List.of(), caretaker);

    assertThat(slots).hasSize(1);
    ElasticSlotDocument slot = slots.get(0);
    
    assertThat(slot.getCaretakerId()).isEqualTo("caretaker-1");
    assertThat(slot.getCaretakerFirstName()).isEqualTo("Іван");
    assertThat(slot.getCaretakerLastName()).isEqualTo("Шевченко");
    assertThat(slot.getCaretakerRating()).isEqualTo(5);
    assertThat(slot.getCaretakerCityId()).isEqualTo("city-1");
    assertThat(slot.getCaretakerCityName()).isEqualTo("Київ");
    assertThat(slot.getDate()).isEqualTo(testDate);
    assertThat(slot.getPricingConfig()).isNotNull();
    assertThat(slot.getPricingConfig().getCurrency()).isEqualTo("UAH");
    assertThat(slot.getPricingConfig().getBasePricePerMinDuration()).isEqualTo(BigDecimal.valueOf(200));
  }
}
