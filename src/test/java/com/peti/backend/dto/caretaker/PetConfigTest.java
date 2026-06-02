package com.peti.backend.dto.caretaker;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PetConfigTest {

  private static final PriceInfo FLAT_PRICE = new PriceInfo(
      BigDecimal.valueOf(100), BigDecimal.valueOf(20), null, "UAH", null);

  private static final PriceInfo SMALL_PRICE = new PriceInfo(
      BigDecimal.valueOf(50), BigDecimal.valueOf(10), null, "UAH", null);

  private static final PriceInfo LARGE_PRICE = new PriceInfo(
      BigDecimal.valueOf(150), BigDecimal.valueOf(30), null, "UAH", null);

  @Test
  @DisplayName("resolvePriceForWeight - sameForAllWeights=true returns petPrice")
  void sameForAllWeights_returnsPetPrice() {
    PetConfig config = new PetConfig(true, new BreedConfig(true, List.of(), List.of()),
        true, FLAT_PRICE, EnumSet.noneOf(DeliveryOption.class), new EnumMap<>(WeightTier.class));

    assertThat(config.resolvePriceForWeight(5)).isEqualTo(FLAT_PRICE);
    assertThat(config.resolvePriceForWeight(50)).isEqualTo(FLAT_PRICE);
  }

  @Test
  @DisplayName("resolvePriceForWeight - uses weight tier when sameForAllWeights=false")
  void usesWeightTier() {
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.SMALL, new WeightTierConfig(true, SMALL_PRICE));
    tiers.put(WeightTier.LARGE, new WeightTierConfig(true, LARGE_PRICE));

    PetConfig config = new PetConfig(true, new BreedConfig(true, List.of(), List.of()),
        false, null, EnumSet.noneOf(DeliveryOption.class), tiers);

    assertThat(config.resolvePriceForWeight(5)).isEqualTo(SMALL_PRICE);
    assertThat(config.resolvePriceForWeight(35)).isEqualTo(LARGE_PRICE);
  }

  @Test
  @DisplayName("resolvePriceForWeight - returns null for disabled tier")
  void disabledTier_returnsNull() {
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.SMALL, new WeightTierConfig(false, SMALL_PRICE));

    PetConfig config = new PetConfig(true, new BreedConfig(true, List.of(), List.of()),
        false, null, EnumSet.noneOf(DeliveryOption.class), tiers);

    assertThat(config.resolvePriceForWeight(5)).isNull();
  }

  @Test
  @DisplayName("resolvePriceForWeight - returns null when tier missing")
  void missingTier_returnsNull() {
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.SMALL, new WeightTierConfig(true, SMALL_PRICE));

    PetConfig config = new PetConfig(true, new BreedConfig(true, List.of(), List.of()),
        false, null, EnumSet.noneOf(DeliveryOption.class), tiers);

    assertThat(config.resolvePriceForWeight(35)).isNull();
  }
}

