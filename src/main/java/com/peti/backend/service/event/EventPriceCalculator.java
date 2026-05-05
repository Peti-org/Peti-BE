package com.peti.backend.service.event;

import com.peti.backend.dto.PriceDto;
import com.peti.backend.dto.PriceItem;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PetConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.WeightTier;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Pet;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Computes the {@link PriceDto} for an event based on the {@link CaretakerPreferences}.
 *
 * <p>For each pet the price is resolved from the caretaker's service config
 * using {@code PriceInfo.priceForService} matched by pet type (breed's petType).
 * The breakdown contains one item per pet (e.g. "BASE_PET_1", "BASE_PET_2").
 */
@Component
public class EventPriceCalculator {

  public static final String BASE_PET_PREFIX = "BASE_PET_";

  /**
   * @param caretaker the caretaker whose preferences define pricing
   * @param slotType  the service type (e.g. "WALKING")
   * @param pets      pets participating in the event
   * @return calculated price with per-pet breakdown
   */
  public PriceDto calculate(Caretaker caretaker, String slotType, List<Pet> pets) {
    if (caretaker == null) {
      throw new IllegalArgumentException("Caretaker must not be null");
    }
    if (pets == null || pets.isEmpty()) {
      throw new IllegalArgumentException("Pets list must not be empty");
    }

    ServiceConfig serviceConfig = resolveServiceConfig(caretaker.getCaretakerPreference(), slotType);
    String currency = resolveCurrency(serviceConfig);

    List<PriceItem> breakdown = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;

    for (int i = 0; i < pets.size(); i++) {
      Pet pet = pets.get(i);
      BigDecimal petPrice = resolvePriceForPet(serviceConfig, pet);
      breakdown.add(new PriceItem(BASE_PET_PREFIX + (i + 1), petPrice));
      total = total.add(petPrice);
    }

    return new PriceDto(total, currency, breakdown);
  }

  private ServiceConfig resolveServiceConfig(CaretakerPreferences preferences, String slotType) {
    if (preferences == null || preferences.services() == null) {
      throw new IllegalStateException("Caretaker has no preferences configured");
    }
    return preferences.services().stream()
        .filter(s -> s.type().name().equals(slotType))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            "Caretaker has no service config for type: " + slotType));
  }

  /**
   * Resolves price for a single pet from the service config.
   * Uses petPrice override first, then falls back to first available weight tier.
   */
  //todo need to swap order and make mmore clearer price getting logic: 1) petPrice override, 2) weight tier matching pet weight (currently just first enabled tier)
  BigDecimal resolvePriceForPet(ServiceConfig config, Pet pet) {
    String petType = pet.getBreed() != null ? pet.getBreed().getPetType() : null;
    if (petType == null || config.configs() == null) {
      return BigDecimal.ZERO;
    }
    PetConfig petConfig = config.configs().get(petType);
    if (petConfig == null || !petConfig.enabled()) {
      return BigDecimal.ZERO;
    }
    // petPrice override takes precedence
    if (petConfig.petPrice() != null) {
      return petConfig.petPrice().priceForService();
    }
    // Fall back to weight tiers — pick first enabled tier (future: match by pet weight)
    Map<String, WeightTier> tiers = petConfig.weightTiers();
    if (tiers == null || tiers.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return tiers.values().stream()
        .filter(WeightTier::enabled)
        .map(t -> t.tierPrice().priceForService())
        .findFirst()
        .orElse(BigDecimal.ZERO);
  }

  private String resolveCurrency(ServiceConfig config) {
    if (config.configs() == null || config.configs().isEmpty()) {
      return "UAH";
    }
    return config.configs().values().stream()
        .filter(PetConfig::enabled)
        .map(pc -> {
          if (pc.petPrice() != null) {
            return pc.petPrice().currency();
          }
          if (pc.weightTiers() != null) {
            return pc.weightTiers().values().stream()
                .filter(WeightTier::enabled)
                .map(t -> t.tierPrice().currency())
                .findFirst().orElse(null);
          }
          return null;
        })
        .filter(java.util.Objects::nonNull)
        .findFirst()
        .orElse("UAH");
  }
}
