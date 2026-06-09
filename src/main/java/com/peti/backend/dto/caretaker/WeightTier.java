package com.peti.backend.dto.caretaker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeightTier {

  SMALL(0, 10),
  MEDIUM(10, 30),
  LARGE(30, 200);

  private final int minKgInclusive;
  private final int maxKgExclusive;

  public boolean matches(double weightKg) {
    return weightKg >= minKgInclusive && weightKg < maxKgExclusive;
  }

  public static WeightTier fromWeight(double weightKg) {
    for (WeightTier tier : values()) {
      if (tier.matches(weightKg)) {
        return tier;
      }
    }
    return LARGE;
  }
}

