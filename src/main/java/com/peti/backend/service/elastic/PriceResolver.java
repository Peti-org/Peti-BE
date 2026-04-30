package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences.PetConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PriceInfo;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.WeightTier;
import com.peti.backend.model.elastic.model.PetInfo;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Resolves pricing information from a {@link ServiceConfig} for a list of pets.
 *
 * <p>The most expensive pet (by tier price) is the "main" pet; all other pets
 * contribute their tier prices as extras.
 */
@Component
public class PriceResolver {

  public BigDecimal resolveBasePrice(ServiceConfig config, List<PetInfo> pets) {
    return maxPriceField(config, pets, PriceInfo::priceForService);
  }

  public BigDecimal resolveStepPrice(ServiceConfig config, List<PetInfo> pets) {
    return maxPriceField(config, pets, PriceInfo::pricePerExtendingStep);
  }

  public String resolveCurrency(ServiceConfig config, List<PetInfo> pets) {
    return pets.stream()
        .map(p -> resolvePriceInfo(config, p))
        .filter(Optional::isPresent).map(Optional::get)
        .map(PriceInfo::currency)
        .findFirst()
        .orElse("UAH");
  }

  /** All pets except the most-expensive one are charged at their tier price. */
  public BigDecimal calculateExtraPetsPrice(ServiceConfig config, List<PetInfo> pets) {
    if (pets == null || pets.size() <= 1) {
      return BigDecimal.ZERO;
    }
    return pets.stream()
        .map(p -> resolvePriceInfo(config, p))
        .filter(Optional::isPresent).map(Optional::get)
        .map(PriceInfo::priceForService)
        .sorted(Comparator.reverseOrder())
        .skip(1)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /** Resolve {@link PriceInfo} for a pet: petPrice override first, then weight-tier lookup. */
  Optional<PriceInfo> resolvePriceInfo(ServiceConfig config, PetInfo pet) {
    if (config.configs() == null) {
      return Optional.empty();
    }
    PetConfig petConfig = config.configs().get(pet.animalType());
    if (petConfig == null || !petConfig.enabled()) {
      return Optional.empty();
    }
    if (petConfig.petPrice() != null) {
      return Optional.of(petConfig.petPrice());
    }
    Map<String, WeightTier> tiers = petConfig.weightTiers();
    if (tiers == null) {
      return Optional.empty();
    }
    WeightTier tier = tiers.get(pet.weightCategory());
    if (tier == null || !tier.enabled()) {
      return Optional.empty();
    }
    return Optional.of(tier.tierPrice());
  }

  /** Provider tax rate — reserved for future use; {@link ServiceConfig} has no tax field yet. */
  public BigDecimal resolveProviderTaxRate(ServiceConfig config) {
    return null;
  }

  // ── internal helper ───────────────────────────────────────────────────────

  private BigDecimal maxPriceField(
      ServiceConfig config,
      List<PetInfo> pets,
      java.util.function.Function<PriceInfo, BigDecimal> fieldExtractor
  ) {
    return pets.stream()
        .map(p -> resolvePriceInfo(config, p))
        .filter(Optional::isPresent).map(Optional::get)
        .map(fieldExtractor)
        .max(Comparator.naturalOrder())
        .orElse(BigDecimal.ZERO);
  }
}

