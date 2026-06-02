package com.peti.backend.dto.caretaker;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeightTierTest {

  @ParameterizedTest
  @CsvSource({"0, SMALL", "5, SMALL", "9.9, SMALL", "10, MEDIUM", "20, MEDIUM", "29.9, MEDIUM", "30, LARGE", "100, LARGE"})
  @DisplayName("fromWeight resolves correct tier")
  void fromWeight(double weight, WeightTier expected) {
    assertThat(WeightTier.fromWeight(weight)).isEqualTo(expected);
  }

  @Test
  @DisplayName("matches checks boundaries correctly")
  void matches() {
    assertThat(WeightTier.SMALL.matches(5)).isTrue();
    assertThat(WeightTier.SMALL.matches(10)).isFalse();
    assertThat(WeightTier.MEDIUM.matches(10)).isTrue();
    assertThat(WeightTier.MEDIUM.matches(30)).isFalse();
    assertThat(WeightTier.LARGE.matches(30)).isTrue();
  }
}

