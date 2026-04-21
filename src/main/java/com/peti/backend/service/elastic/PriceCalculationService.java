package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.service.elastic.model.PetInfo;
import com.peti.backend.service.elastic.model.PriceBreakdown;
import com.peti.backend.service.elastic.model.PriceCalculationResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Calculates slot prices using the caretaker's {@link ServiceConfig}.
 *
 * <p>Formula: ((base + steps × stepPrice + extraPets) × (1 + providerTax)) × (1 + feeRate) + feeFixed
 */
@Service
public class PriceCalculationService {

  private final BigDecimal serviceFeeRate;
  private final BigDecimal serviceFeeFixed;
  private final PriceResolver priceResolver;

  public PriceCalculationService(
      @Value("${peti.pricing.service-fee-rate:0.10}") BigDecimal serviceFeeRate,
      @Value("${peti.pricing.service-fee-fixed:20.00}") BigDecimal serviceFeeFixed,
      PriceResolver priceResolver
  ) {
    this.serviceFeeRate = serviceFeeRate;
    this.serviceFeeFixed = serviceFeeFixed;
    this.priceResolver = priceResolver;
  }

  public PriceCalculationResult calculatePrice(ElasticSlotDocument slot, List<PetInfo> pets) {
    ServiceConfig config = slot.getServiceConfig();
    if (config == null) {
      return PriceCalculationResult.error("Service configuration not available");
    }

    long durationMinutes = slot.getDurationMinutes();
    if (durationMinutes <= 0) {
      return PriceCalculationResult.error("Invalid slot duration");
    }

    long minDuration = config.baseDuration().toMinutes();
    long stepMinutes = config.stepDuration().toMinutes();

    if (durationMinutes < minDuration) {
      return PriceCalculationResult.error(
          "Slot duration (%d min) is less than minimum (%d min)".formatted(durationMinutes, minDuration));
    }

    long extraMinutes = durationMinutes - minDuration;
    if (stepMinutes > 0 && extraMinutes % stepMinutes != 0) {
      return PriceCalculationResult.error(
          "Duration must be base duration + multiple of step (%d min)".formatted(stepMinutes));
    }

    int steps = stepMinutes > 0 ? (int) (extraMinutes / stepMinutes) : 0;

    BigDecimal basePrice      = priceResolver.resolveBasePrice(config, pets);
    BigDecimal stepsPrice     = priceResolver.resolveStepPrice(config, pets).multiply(BigDecimal.valueOf(steps));
    BigDecimal extraPetsPrice = priceResolver.calculateExtraPetsPrice(config, pets);
    String currency           = priceResolver.resolveCurrency(config, pets);

    BigDecimal subtotal = basePrice.add(stepsPrice).add(extraPetsPrice);

    BigDecimal providerTaxRate   = priceResolver.resolveProviderTaxRate(config);
    BigDecimal providerTaxAmount = BigDecimal.ZERO;
    BigDecimal afterProviderTax  = subtotal;

    if (providerTaxRate != null && providerTaxRate.compareTo(BigDecimal.ZERO) > 0) {
      providerTaxAmount = subtotal.multiply(providerTaxRate).setScale(2, RoundingMode.HALF_UP);
      afterProviderTax = subtotal.add(providerTaxAmount);
    }

    BigDecimal serviceFeeAmount = afterProviderTax.multiply(serviceFeeRate).setScale(2, RoundingMode.HALF_UP);
    BigDecimal total = afterProviderTax.add(serviceFeeAmount).add(serviceFeeFixed);

    return PriceCalculationResult.success(PriceBreakdown.builder()
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
        .currency(currency)
        .durationMinutes(durationMinutes)
        .petsCount(pets.size())
        .build()
    );
  }
}
