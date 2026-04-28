package com.peti.backend.service.elastic.model;

import java.math.BigDecimal;

/**
 * Detailed breakdown of a calculated slot price.
 */
@lombok.Builder
public record PriceBreakdown(
    BigDecimal basePrice,
    int stepsCount,
    BigDecimal stepsPrice,
    BigDecimal extraPetsPrice,
    BigDecimal subtotal,
    BigDecimal providerTaxRate,
    BigDecimal providerTaxAmount,
    BigDecimal serviceFeeRate,
    BigDecimal serviceFeeAmount,
    BigDecimal serviceFeeFixed,
    BigDecimal total,
    String currency,
    long durationMinutes,
    int petsCount
) {}
