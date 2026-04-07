package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.ExtraPetPrice;
import com.peti.backend.model.elastic.ElasticSlotDocument.PricingConfig;
import com.peti.backend.service.elastic.PriceCalculationService.PetInfo;
import com.peti.backend.service.elastic.PriceCalculationService.PriceBreakdown;
import com.peti.backend.service.elastic.PriceCalculationService.PriceCalculationResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceCalculationServiceTest {

  private PriceCalculationService service;

  @BeforeEach
  void setUp() {
    // Service fee: 10% + 20 UAH
    service = new PriceCalculationService(
        BigDecimal.valueOf(0.10),
        BigDecimal.valueOf(20)
    );
  }

  @Test
  @DisplayName("Basic price calculation - minimum duration, one pet")
  void basicPriceCalculation_minDuration_onePet() {
    // Given: 60 min slot, min duration 60, base price 100
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),  // 60 minutes
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .currency("UAH")
            .build()
    );
    List<PetInfo> pets = List.of(new PetInfo("dog", "medium"));

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then: 100 * 1.1 + 20 = 130
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    assertThat(breakdown.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    assertThat(breakdown.stepsCount()).isEqualTo(0);
    assertThat(breakdown.stepsPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(breakdown.extraPetsPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(breakdown.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(100));
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(130));
  }

  @Test
  @DisplayName("Price calculation with extra steps")
  void priceCalculation_withExtraSteps() {
    // Given: 90 min slot, min duration 60, step 15, base 100, step price 30
    // Steps = (90 - 60) / 15 = 2
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 30),  // 90 minutes
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .currency("UAH")
            .build()
    );
    List<PetInfo> pets = List.of(new PetInfo("dog", "medium"));

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then: (100 + 2*30) * 1.1 + 20 = 160 * 1.1 + 20 = 176 + 20 = 196
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    assertThat(breakdown.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    assertThat(breakdown.stepsCount()).isEqualTo(2);
    assertThat(breakdown.stepsPrice()).isEqualByComparingTo(BigDecimal.valueOf(60));
    assertThat(breakdown.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(160));
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(196));
  }

  @Test
  @DisplayName("Price calculation with two pets - extra pet charge")
  void priceCalculation_withTwoPets() {
    // Given: 60 min slot, base 100, dog=80 extra, cat=60 extra
    // With dog + cat, cheapest (cat 60) is extra
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .extraPetPrices(List.of(
                ExtraPetPrice.builder().animalType("dog").weightCategory("medium").price(BigDecimal.valueOf(80)).build(),
                ExtraPetPrice.builder().animalType("cat").weightCategory("medium").price(BigDecimal.valueOf(60)).build()
            ))
            .currency("UAH")
            .build()
    );
    List<PetInfo> pets = List.of(
        new PetInfo("dog", "medium"),
        new PetInfo("cat", "medium")
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then: (100 + 60) * 1.1 + 20 = 160 * 1.1 + 20 = 176 + 20 = 196
    // The most expensive pet (dog=80) is "main", cat (60) is extra
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    assertThat(breakdown.extraPetsPrice()).isEqualByComparingTo(BigDecimal.valueOf(60));
    assertThat(breakdown.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(160));
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(196));
  }

  @Test
  @DisplayName("Price calculation with provider tax")
  void priceCalculation_withProviderTax() {
    // Given: 60 min, base 100, provider tax 20%
    // Formula: ((100) * 1.2) * 1.1 + 20 = 120 * 1.1 + 20 = 132 + 20 = 152
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .providerTaxRate(BigDecimal.valueOf(0.20))
            .currency("UAH")
            .build()
    );
    List<PetInfo> pets = List.of(new PetInfo("dog", "medium"));

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    assertThat(breakdown.providerTaxRate()).isEqualByComparingTo(BigDecimal.valueOf(0.20));
    assertThat(breakdown.providerTaxAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(152));
  }

  @Test
  @DisplayName("Full complex price calculation")
  void fullComplexPriceCalculation() {
    // Given: User's example:
    // 100 (base) + 4*50 (steps) + 60 (extra pet) = 360
    // (360 * 1.2) * 1.1 + 20 = 432 * 1.1 + 20 = 475.2 + 20 = 495.2
    
    // Slot: min 60, step 10, base 100, step price 50
    // Duration: 100 min = 60 + 4*10, so 4 steps
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 40),  // 100 minutes
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(10)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(50))
            .extraPetPrices(List.of(
                ExtraPetPrice.builder().animalType("dog").weightCategory("medium").price(BigDecimal.valueOf(80)).build(),
                ExtraPetPrice.builder().animalType("cat").weightCategory("medium").price(BigDecimal.valueOf(60)).build()
            ))
            .providerTaxRate(BigDecimal.valueOf(0.20))
            .currency("UAH")
            .build()
    );
    List<PetInfo> pets = List.of(
        new PetInfo("dog", "medium"),
        new PetInfo("cat", "medium")
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    
    assertThat(breakdown.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(100));
    assertThat(breakdown.stepsCount()).isEqualTo(4);
    assertThat(breakdown.stepsPrice()).isEqualByComparingTo(BigDecimal.valueOf(200));
    assertThat(breakdown.extraPetsPrice()).isEqualByComparingTo(BigDecimal.valueOf(60));
    assertThat(breakdown.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(360));
    assertThat(breakdown.providerTaxAmount()).isEqualByComparingTo(BigDecimal.valueOf(72)); // 360 * 0.2
    // After provider tax: 432
    assertThat(breakdown.serviceFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(43.20)); // 432 * 0.1
    // Total: 432 + 43.2 + 20 = 495.2
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(495.20));
  }

  @Test
  @DisplayName("Duration less than minimum returns error")
  void durationLessThanMinimum_returnsError() {
    // Given: 30 min slot, min duration 60
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(10, 30),  // 30 minutes
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .currency("UAH")
            .build()
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, List.of(new PetInfo("dog", "medium")));

    // Then
    assertThat(result.success()).isFalse();
    assertThat(result.error()).contains("less than minimum");
  }

  @Test
  @DisplayName("Duration not divisible by step returns error")
  void durationNotDivisibleByStep_returnsError() {
    // Given: 70 min slot, min 60, step 15 -> (70-60)/15 = 0.67, not integer
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 10),  // 70 minutes
        PricingConfig.builder()
            .minDurationMinutes(60)
            .stepMinutes(15)
            .basePricePerMinDuration(BigDecimal.valueOf(100))
            .pricePerStep(BigDecimal.valueOf(30))
            .currency("UAH")
            .build()
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, List.of(new PetInfo("dog", "medium")));

    // Then
    assertThat(result.success()).isFalse();
    assertThat(result.error()).contains("multiple of step");
  }

  @Test
  @DisplayName("Missing pricing config returns error")
  void missingPricingConfig_returnsError() {
    // Given
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        null
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, List.of(new PetInfo("dog", "medium")));

    // Then
    assertThat(result.success()).isFalse();
    assertThat(result.error()).contains("not available");
  }

  private ElasticSlotDocument createSlot(LocalTime timeFrom, LocalTime timeTo, PricingConfig pricingConfig) {
    return ElasticSlotDocument.builder()
        .id("slot-1")
        .caretakerId("caretaker-1")
        .date(LocalDate.of(2026, 2, 15))
        .timeFrom(timeFrom)
        .timeTo(timeTo)
        .pricingConfig(pricingConfig)
        .capacity(3)
        .build();
  }
}
