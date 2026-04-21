package com.peti.backend.service.elastic.model;

/**
 * Result of a price calculation — either a success with breakdown or an error message.
 */
public record PriceCalculationResult(
    boolean success,
    String error,
    PriceBreakdown breakdown
) {

  public static PriceCalculationResult success(PriceBreakdown breakdown) {
    return new PriceCalculationResult(true, null, breakdown);
  }

  public static PriceCalculationResult error(String message) {
    return new PriceCalculationResult(false, message, null);
  }
}

