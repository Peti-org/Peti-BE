package com.peti.backend.dto.caretaker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.Nullable;

@Schema(description = "Per-pet-type configuration within a service")
public record PetConfig(
    @Schema(description = "Whether this pet type is enabled for the service")
    boolean enabled,

    @Schema(description = "Breed inclusion/exclusion configuration")
    @NotNull(message = "Breed config must not be null")
    @Valid
    BreedConfig breeds,

    @Schema(description = "When true, petPrice applies regardless of weight tier")
    boolean sameForAllWeights,

    @Schema(description = "Flat price used when sameForAllWeights=true", nullable = true)
    @Nullable
    @Valid
    PriceInfo petPrice,

    @Schema(description = "Delivery options offered for this pet type")
    @NotNull(message = "Delivery options must not be null")
    Set<DeliveryOption> deliveryOptions,

    @Schema(description = "Weight tier pricing, used when sameForAllWeights=false", nullable = true)
    @Nullable
    @Valid
    Map<WeightTier, WeightTierConfig> weightTiers
) {

  public PetConfig {
    if (deliveryOptions == null) {
      deliveryOptions = EnumSet.noneOf(DeliveryOption.class);
    }
    if (weightTiers == null) {
      weightTiers = new EnumMap<>(WeightTier.class);
    }
  }

  /**
   * Resolves the effective PriceInfo for a given weight.
   * When sameForAllWeights is true, returns petPrice.
   * Otherwise looks up the matching weight tier.
   */
  public PriceInfo resolvePriceForWeight(double weightKg) {
    if (sameForAllWeights) {
      return petPrice;
    }
    WeightTier tier = WeightTier.fromWeight(weightKg);
    WeightTierConfig tierConfig = weightTiers.get(tier);
    if (tierConfig == null || !tierConfig.enabled()) {
      return null;
    }
    return tierConfig.tierPrice();
  }
}

