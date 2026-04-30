package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences.BreedConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PetConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PickupDelivery;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PriceInfo;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.WeightTier;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.PetInfo;
import java.math.BigDecimal;
import java.time.Duration;
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
        "dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfig("small", BigDecimal.valueOf(60), BigDecimal.valueOf(20))
    ));
    List<PetInfo> pets = List.of(new PetInfo("dog", "medium"), new PetInfo("cat", "small"));
    assertThat(resolver.resolveBasePrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(100));
  }

  @Test
  @DisplayName("resolveStepPrice - picks the most expensive pet")
  void resolveStepPrice() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfig("small", BigDecimal.valueOf(60), BigDecimal.valueOf(20))
    ));
    List<PetInfo> pets = List.of(new PetInfo("dog", "medium"), new PetInfo("cat", "small"));
    assertThat(resolver.resolveStepPrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(30));
  }

  @Test
  @DisplayName("calculateExtraPetsPrice - sums all except most expensive")
  void extraPetsPrice() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfig("large", BigDecimal.valueOf(100), BigDecimal.valueOf(30)),
        "cat", petConfig("small", BigDecimal.valueOf(60), BigDecimal.valueOf(20)),
        "bird", petConfig("small", BigDecimal.valueOf(40), BigDecimal.valueOf(10))
    ));
    List<PetInfo> pets = List.of(
        new PetInfo("dog", "large"),
        new PetInfo("cat", "small"),
        new PetInfo("bird", "small")
    );
    // Most expensive = 100 (dog), extras = 60 + 40 = 100
    assertThat(resolver.calculateExtraPetsPrice(config, pets)).isEqualByComparingTo(BigDecimal.valueOf(100));
  }

  @Test
  @DisplayName("calculateExtraPetsPrice - single pet returns zero")
  void extraPetsPrice_singlePet() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))
    ));
    assertThat(resolver.calculateExtraPetsPrice(config, List.of(new PetInfo("dog", "medium"))))
        .isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("resolvePriceInfo - uses petPrice override when present")
  void petPriceOverride() {
    PriceInfo override = new PriceInfo(BigDecimal.valueOf(200), BigDecimal.valueOf(50), null, "UAH", null);
    PetConfig petConfig = new PetConfig(
        true,
        new BreedConfig(true, List.of(), List.of()),
        true,
        override,
        new PickupDelivery(true, false),
        Map.of("medium", new WeightTier(
            new PriceInfo(BigDecimal.valueOf(100), BigDecimal.valueOf(30), null, "UAH", null), true))
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    Optional<PriceInfo> result = resolver.resolvePriceInfo(config, new PetInfo("dog", "medium"));
    assertThat(result).isPresent();
    assertThat(result.get().priceForService()).isEqualByComparingTo(BigDecimal.valueOf(200));
  }

  @Test
  @DisplayName("resolvePriceInfo - falls back to weight tier")
  void weightTierFallback() {
    PetConfig petConfig = new PetConfig(
        true,
        new BreedConfig(true, List.of(), List.of()),
        false,
        null,
        new PickupDelivery(true, false),
        Map.of("large", new WeightTier(
            new PriceInfo(BigDecimal.valueOf(150), BigDecimal.valueOf(40), null, "UAH", null), true))
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    Optional<PriceInfo> result = resolver.resolvePriceInfo(config, new PetInfo("dog", "large"));
    assertThat(result).isPresent();
    assertThat(result.get().priceForService()).isEqualByComparingTo(BigDecimal.valueOf(150));
  }

  @Test
  @DisplayName("resolvePriceInfo - disabled pet returns empty")
  void disabledPet() {
    PetConfig petConfig = new PetConfig(
        false, new BreedConfig(true, List.of(), List.of()),
        true, null, new PickupDelivery(true, false), Map.of()
    );
    ServiceConfig config = serviceConfigWith(Map.of("dog", petConfig));

    assertThat(resolver.resolvePriceInfo(config, new PetInfo("dog", "medium"))).isEmpty();
  }

  @Test
  @DisplayName("resolvePriceInfo - unknown animal type returns empty")
  void unknownAnimalType() {
    ServiceConfig config = serviceConfigWith(Map.of());
    assertThat(resolver.resolvePriceInfo(config, new PetInfo("dragon", "medium"))).isEmpty();
  }

  @Test
  @DisplayName("resolveCurrency returns first available currency")
  void resolveCurrency() {
    ServiceConfig config = serviceConfigWith(Map.of(
        "dog", petConfig("medium", BigDecimal.valueOf(100), BigDecimal.valueOf(30))
    ));
    assertThat(resolver.resolveCurrency(config, List.of(new PetInfo("dog", "medium")))).isEqualTo("UAH");
  }

  @Test
  @DisplayName("resolveProviderTaxRate returns null (reserved)")
  void providerTaxRate() {
    assertThat(resolver.resolveProviderTaxRate(null)).isNull();
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private ServiceConfig serviceConfigWith(Map<String, PetConfig> configs) {
    return new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3,
        Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
        configs, List.of()
    );
  }

  private PetConfig petConfig(String weightCategory, BigDecimal basePrice, BigDecimal stepPrice) {
    PriceInfo price = new PriceInfo(basePrice, stepPrice, null, "UAH", null);
    return new PetConfig(
        true,
        new BreedConfig(true, List.of(), List.of()),
        false,
        null,
        new PickupDelivery(true, false),
        Map.of(weightCategory, new WeightTier(price, true))
    );
  }
}

