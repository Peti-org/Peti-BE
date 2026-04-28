package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences.BreedConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PetConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PickupDelivery;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PriceInfo;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.WeightTier;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.service.elastic.model.PetInfo;
import com.peti.backend.service.elastic.model.PriceBreakdown;
import com.peti.backend.service.elastic.model.PriceCalculationResult;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
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
        BigDecimal.valueOf(20),
        new PriceResolver()
    );
  }

  @Test
  @DisplayName("Basic price calculation - minimum duration, one pet")
  void basicPriceCalculation_minDuration_onePet() {
    // Given: 60 min slot, min duration 60, base price 100, step price 30
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),  // 60 minutes
        serviceConfig(Duration.ofMinutes(60), Duration.ofMinutes(15),
            Map.of("dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))))
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
        serviceConfig(Duration.ofMinutes(60), Duration.ofMinutes(15),
            Map.of("dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))))
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
    // Given: 60 min slot, dog base=80, cat base=60
    // Most expensive (dog 80) is main; cat (60) is extra
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(11, 0),
        serviceConfig(Duration.ofMinutes(60), Duration.ofMinutes(15),
            Map.of(
                "dog", petConfig("medium", BigDecimal.valueOf(80), BigDecimal.valueOf(30)),
                "cat", petConfig("medium", BigDecimal.valueOf(60), BigDecimal.valueOf(20))
            ))
    );
    List<PetInfo> pets = List.of(
        new PetInfo("dog", "medium"),
        new PetInfo("cat", "medium")
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, pets);

    // Then: base=80, extra=60, subtotal=140, (140*1.1)+20 = 174
    assertThat(result.success()).isTrue();
    PriceBreakdown breakdown = result.breakdown();
    assertThat(breakdown.basePrice()).isEqualByComparingTo(BigDecimal.valueOf(80));
    assertThat(breakdown.extraPetsPrice()).isEqualByComparingTo(BigDecimal.valueOf(60));
    assertThat(breakdown.subtotal()).isEqualByComparingTo(BigDecimal.valueOf(140));
    assertThat(breakdown.total()).isEqualByComparingTo(BigDecimal.valueOf(174));
  }

  @Test
  @DisplayName("Duration less than minimum returns error")
  void durationLessThanMinimum_returnsError() {
    // Given: 30 min slot, min duration 60
    ElasticSlotDocument slot = createSlot(
        LocalTime.of(10, 0),
        LocalTime.of(10, 30),  // 30 minutes
        serviceConfig(Duration.ofMinutes(60), Duration.ofMinutes(15),
            Map.of("dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))))
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
        serviceConfig(Duration.ofMinutes(60), Duration.ofMinutes(15),
            Map.of("dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))))
    );

    // When
    PriceCalculationResult result = service.calculatePrice(slot, List.of(new PetInfo("dog", "medium")));

    // Then
    assertThat(result.success()).isFalse();
    assertThat(result.error()).contains("multiple of step");
  }

  @Test
  @DisplayName("Missing service config returns error")
  void missingServiceConfig_returnsError() {
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

  // ── helpers ───────────────────────────────────────────────────────────────

  private ElasticSlotDocument createSlot(LocalTime timeFrom, LocalTime timeTo, ServiceConfig serviceConfig) {
    return ElasticSlotDocument.builder()
        .id("slot-1")
        .caretakerId("caretaker-1")
        .date(LocalDate.of(2026, 2, 15))
        .timeFrom(timeFrom)
        .timeTo(timeTo)
        .serviceConfig(serviceConfig)
        .capacity(3)
        .build();
  }

  private ServiceConfig serviceConfig(Duration baseDuration, Duration stepDuration,
                                       Map<String, PetConfig> configs) {
    return new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3,
        baseDuration, stepDuration, Duration.ofHours(2),
        configs, List.of("feeding")
    );
  }

  private PetConfig petConfig(String weightCategory, BigDecimal basePrice, BigDecimal stepPrice) {
    PriceInfo priceInfo = new PriceInfo(basePrice, stepPrice, null, "UAH", null);
    WeightTier tier = new WeightTier(priceInfo, true);
    BreedConfig breeds = new BreedConfig(true, List.of(), List.of());
    PickupDelivery pickup = new PickupDelivery(false, false);
    return new PetConfig(true, breeds, false, null, pickup, Map.of(weightCategory, tier));
  }
}
