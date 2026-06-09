package com.peti.backend.service.slot.search;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretaker.BreedConfig;
import com.peti.backend.dto.caretaker.DeliveryOption;
import com.peti.backend.dto.caretaker.PetConfig;
import com.peti.backend.dto.caretaker.PriceInfo;
import com.peti.backend.dto.caretaker.ServiceConfig;
import com.peti.backend.dto.caretaker.WeightTier;
import com.peti.backend.dto.caretaker.WeightTierConfig;
import com.peti.backend.model.elastic.model.PetInfo;
import com.peti.backend.model.internal.ServiceType;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PriceResolverTest {

  private PriceResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new PriceResolver();
  }

  @Test
  @DisplayName("resolveBasePrice - picks the most expensive pet")
  void resolveBasePrice_mostExpensive() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfigForTier(WeightTier.MEDIUM, BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfigForTier(WeightTier.SMALL, BigDecimal.valueOf(60), BigDecimal.valueOf(20))
    ));
    List<PetInfo> pets = List.of(new PetInfo("dog", "MEDIUM"), new PetInfo("cat", "SMALL"));
    assertThat(resolver.resolveBasePrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(100));
  }

  @Test
  @DisplayName("resolveStepPrice - picks the most expensive pet")
  void resolveStepPrice() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfigForTier(WeightTier.MEDIUM, BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfigForTier(WeightTier.SMALL, BigDecimal.valueOf(60), BigDecimal.valueOf(20))
    ));
    List<PetInfo> pets = List.of(new PetInfo("dog", "MEDIUM"), new PetInfo("cat", "SMALL"));
    assertThat(resolver.resolveStepPrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(30));
  }

  @Test
  @DisplayName("calculateExtraPetsPrice - sums all except most expensive")
  void extraPetsPrice() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfigForTier(WeightTier.LARGE, BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfigForTier(WeightTier.SMALL, BigDecimal.valueOf(60), BigDecimal.valueOf(20)),
        "bird", petConfigForTier(WeightTier.SMALL, BigDecimal.valueOf(40), BigDecimal.valueOf(10))
    ));
    List<PetInfo> pets = List.of(
        new PetInfo("dog", "LARGE"),
        new PetInfo("cat", "SMALL"),
        new PetInfo("bird", "SMALL")
    );
    assertThat(resolver.calculateExtraPetsPrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(100));
  }

  @Test
  @DisplayName("calculateExtraPetsPrice - single pet returns zero")
  void extraPetsPrice_singlePet() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfigForTier(WeightTier.MEDIUM, BigDecimal.valueOf(100), BigDecimal.valueOf(30))
    ));
    assertThat(resolver.calculateExtraPetsPrice(config, List.of(new PetInfo("dog", "MEDIUM"))))
        .isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("resolvePriceInfo - uses petPrice when sameForAllWeights=true")
  void petPriceOverride() {
    PriceInfo override = new PriceInfo(BigDecimal.valueOf(200), BigDecimal.valueOf(50), null, "UAH", null);
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.MEDIUM, new WeightTierConfig(true,
        new PriceInfo(BigDecimal.valueOf(100), BigDecimal.valueOf(30), null, "UAH", null)));
    PetConfig petConfig = new PetConfig(
        true, new BreedConfig(true, List.of(), List.of()),
        true, override, EnumSet.of(DeliveryOption.PICK_UP), tiers
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    Optional<PriceInfo> result = resolver.resolvePriceInfo(config, new PetInfo("dog", "MEDIUM"));
    assertThat(result).isPresent();
    assertThat(result.get().priceForService()).isEqualByComparingTo(BigDecimal.valueOf(200));
  }

  @Test
  @DisplayName("resolvePriceInfo - falls back to weight tier when sameForAllWeights=false")
  void weightTierFallback() {
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.LARGE, new WeightTierConfig(true,
        new PriceInfo(BigDecimal.valueOf(150), BigDecimal.valueOf(40), null, "UAH", null)));
    PetConfig petConfig = new PetConfig(
        true, new BreedConfig(true, List.of(), List.of()),
        false, null, EnumSet.of(DeliveryOption.PICK_UP), tiers
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    Optional<PriceInfo> result = resolver.resolvePriceInfo(config, new PetInfo("dog", "LARGE"));
    assertThat(result).isPresent();
    assertThat(result.get().priceForService()).isEqualByComparingTo(BigDecimal.valueOf(150));
  }

  @Test
  @DisplayName("resolvePriceInfo - disabled pet returns empty")
  void disabledPet() {
    PetConfig petConfig = new PetConfig(
        false, new BreedConfig(true, List.of(), List.of()),
        true, null, EnumSet.noneOf(DeliveryOption.class), new EnumMap<>(WeightTier.class)
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    assertThat(resolver.resolvePriceInfo(config, new PetInfo("dog", "MEDIUM"))).isEmpty();
  }

  @Test
  @DisplayName("resolvePriceInfo - unknown animal type returns empty")
  void unknownAnimalType() {
    ServiceConfig config = serviceConfigWith(Map.of());
    assertThat(resolver.resolvePriceInfo(config, new PetInfo("dragon", "MEDIUM"))).isEmpty();
  }

  @Test
  @DisplayName("resolveCurrency returns first available currency")
  void resolveCurrency() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfigForTier(WeightTier.MEDIUM, BigDecimal.valueOf(100), BigDecimal.valueOf(30))
    ));
    assertThat(resolver.resolveCurrency(config, List.of(new PetInfo("dog", "MEDIUM")))).isEqualTo("UAH");
  }

  @Test
  @DisplayName("resolveProviderTaxRate returns null (reserved)")
  void providerTaxRate() {
    assertThat(resolver.resolveProviderTaxRate(null)).isNull();
  }

  private ServiceConfig serviceConfigWith(Map<String, PetConfig> configs) {
    return new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3, 20,
        Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
        configs, List.of()
    );
  }

  private PetConfig petConfigForTier(WeightTier tier, BigDecimal basePrice, BigDecimal stepPrice) {
    PriceInfo price = new PriceInfo(basePrice, stepPrice, null, "UAH", null);
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(tier, new WeightTierConfig(true, price));
    return new PetConfig(
        true, new BreedConfig(true, List.of(), List.of()),
        false, null, EnumSet.of(DeliveryOption.PICK_UP), tiers
    );
  }
}

