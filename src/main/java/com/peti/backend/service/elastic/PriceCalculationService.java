package com.peti.backend.service.elastic;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.ExtraPetPrice;
import com.peti.backend.model.elastic.ElasticSlotDocument.PricingConfig;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for calculating slot prices based on complex pricing rules.
 *
 * <p>Pricing formula:
 * ((basePricePerMinDuration + steps * pricePerStep + extraPetPrices) * (1 + providerTaxRate))
 *     * (1 + serviceFeeRate) + serviceFeeFixed
 *
 * <p>Where:
 * - steps = (actualDuration - minDuration) / stepMinutes
 * - extraPetPrices = sum of extra pet prices for all pets except the most expensive one
 *   (sorted by price ascending, cheapest ones are "extra")
 */
@Service
public class PriceCalculationService { // Рандомно нагенерений сервіс, який мав би рахувати ціну за складною формулою. Точно треба буде змінювати це

  private final BigDecimal serviceFeeRate;
  private final BigDecimal serviceFeeFixed;

  public PriceCalculationService(
      @Value("${peti.pricing.service-fee-rate:0.10}") BigDecimal serviceFeeRate,   // Як мінінмум ці штуки
      @Value("${peti.pricing.service-fee-fixed:20.00}") BigDecimal serviceFeeFixed // Як мінінмум ці штуки
  ) {
    this.serviceFeeRate = serviceFeeRate;
    this.serviceFeeFixed = serviceFeeFixed;
  }

  /**
   * Calculate total price for a slot given the requested pets.
   *
   * @param slot the slot document with pricing config
   * @param pets list of pets with their types and weight categories
   * @return calculated price result
   */
  public PriceCalculationResult calculatePrice(ElasticSlotDocument slot, List<PetInfo> pets) {
    PricingConfig config = slot.getPricingConfig();
    if (config == null) {
      return PriceCalculationResult.error("Pricing configuration not available");
    }

    long durationMinutes = slot.getDurationMinutes();
    if (durationMinutes <= 0) {
      return PriceCalculationResult.error("Invalid slot duration");
    }

    // Validate duration against min duration
    if (durationMinutes < config.getMinDurationMinutes()) {
      return PriceCalculationResult.error(
          "Slot duration (%d min) is less than minimum (%d min)"
              .formatted(durationMinutes, config.getMinDurationMinutes()));
    }

    // Calculate number of steps
    long extraMinutes = durationMinutes - config.getMinDurationMinutes();
    if (extraMinutes % config.getStepMinutes() != 0) {
      return PriceCalculationResult.error(
          "Duration must be min duration + multiple of step (%d min)"
              .formatted(config.getStepMinutes()));
    }
    int steps = (int) (extraMinutes / config.getStepMinutes());

    // Base price calculation
    BigDecimal basePrice = config.getBasePricePerMinDuration();
    BigDecimal stepsPrice = config.getPricePerStep()
        .multiply(BigDecimal.valueOf(steps));

    // Extra pets calculation
    // The most expensive pet is "main", others are "extra"
    BigDecimal extraPetsPrice = calculateExtraPetsPrice(config, pets);

    // Subtotal before taxes
    BigDecimal subtotal = basePrice.add(stepsPrice).add(extraPetsPrice);

    // Apply provider tax rate if set
    BigDecimal providerTaxRate = config.getProviderTaxRate();
    BigDecimal afterProviderTax = subtotal;
    BigDecimal providerTaxAmount = BigDecimal.ZERO;
    if (providerTaxRate != null && providerTaxRate.compareTo(BigDecimal.ZERO) > 0) {
      providerTaxAmount = subtotal.multiply(providerTaxRate)
          .setScale(2, RoundingMode.HALF_UP);
      afterProviderTax = subtotal.add(providerTaxAmount);
    }

    // Apply service fee
    BigDecimal serviceFeeAmount = afterProviderTax.multiply(serviceFeeRate)
        .setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = afterProviderTax.add(serviceFeeAmount).add(serviceFeeFixed);

    return PriceCalculationResult.success(
        PriceBreakdown.builder()
            .basePrice(basePrice)
            .stepsCount(steps)
            .stepsPrice(stepsPrice)
            .extraPetsPrice(extraPetsPrice)
            .subtotal(subtotal)
            .providerTaxRate(providerTaxRate)
            .providerTaxAmount(providerTaxAmount)
            .serviceFeeRate(serviceFeeRate)
            .serviceFeeAmount(serviceFeeAmount)
            .serviceFeeFixed(serviceFeeFixed)
            .total(total.setScale(2, RoundingMode.HALF_UP))
            .currency(config.getCurrency())
            .durationMinutes(durationMinutes)
            .petsCount(pets.size())
            .build()
    );
  }

  /**
   * Calculate extra pets price.
   * Sort pets by their extra price descending, the most expensive one is "main" (free),
   * others are charged.
   */
  private BigDecimal calculateExtraPetsPrice(PricingConfig config, List<PetInfo> pets) {
    if (pets == null || pets.size() <= 1) {
      return BigDecimal.ZERO;
    }

    List<ExtraPetPrice> extraPrices = config.getExtraPetPrices();
    if (extraPrices == null || extraPrices.isEmpty()) {
      return BigDecimal.ZERO;
    }

    // Get prices for each pet
    List<BigDecimal> petPrices = pets.stream()
        .map(pet -> findExtraPetPrice(extraPrices, pet))
        .sorted(Comparator.reverseOrder()) // Most expensive first
        .toList();

    // Skip the first (most expensive) pet - it's the "main" one
    // Sum the rest
    return petPrices.stream()
        .skip(1)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Find extra pet price for a specific pet.
   */
  private BigDecimal findExtraPetPrice(List<ExtraPetPrice> extraPrices, PetInfo pet) {
    // Try exact match first
    Optional<ExtraPetPrice> exactMatch = extraPrices.stream()
        .filter(p -> p.getAnimalType().equals(pet.animalType())
            && p.getWeightCategory().equals(pet.weightCategory()))
        .findFirst();

    if (exactMatch.isPresent()) {
      return exactMatch.get().getPrice();
    }

    // Try match by animal type only (any weight)
    Optional<ExtraPetPrice> typeMatch = extraPrices.stream()
        .filter(p -> p.getAnimalType().equals(pet.animalType())
            && "any".equals(p.getWeightCategory()))
        .findFirst();

    return typeMatch.map(ExtraPetPrice::getPrice).orElse(BigDecimal.ZERO);
  }

  /**
   * Pet information for price calculation.
   */
  public record PetInfo(
      String animalType,      // dog, cat, bird, etc.
      String weightCategory   // small, medium, large, extra_large
  ) {}

  /**
   * Result of price calculation.
   */
  public record PriceCalculationResult(
      boolean success,
      String error,
      PriceBreakdown breakdown
  ) {
    public static PriceCalculationResult success(PriceBreakdown breakdown) {
      return new PriceCalculationResult(true, null, breakdown);
    }

    public static PriceCalculationResult error(String error) {
      return new PriceCalculationResult(false, error, null);
    }
  }

  /**
   * Detailed price breakdown for transparency.
   */
  @lombok.Builder
  public record PriceBreakdown(
      BigDecimal basePrice,           // Price for min duration
      int stepsCount,                 // Number of extra steps
      BigDecimal stepsPrice,          // Total price for extra steps
      BigDecimal extraPetsPrice,      // Total extra pets price
      BigDecimal subtotal,            // Before taxes
      BigDecimal providerTaxRate,     // Provider's tax rate (can be null)
      BigDecimal providerTaxAmount,   // Provider's tax amount
      BigDecimal serviceFeeRate,      // Service fee rate
      BigDecimal serviceFeeAmount,    // Service fee calculated
      BigDecimal serviceFeeFixed,     // Fixed service fee
      BigDecimal total,               // Final total
      String currency,
      long durationMinutes,
      int petsCount
  ) {}
}
