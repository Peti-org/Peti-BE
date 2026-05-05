package com.peti.backend.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.pet.PetProfile;
import com.peti.backend.dto.pet.PetProfile.Sex;
import com.peti.backend.dto.pet.PetProfile.TriState;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeepClonerTest {

  private DeepCloner deepCloner;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    deepCloner = new DeepCloner(objectMapper);
  }

  @Test
  @DisplayName("deepCopyPreference creates an independent copy")
  void deepCopyPreference_createsIndependentCopy() {
    CaretakerPreferences original = new CaretakerPreferences(null, null);

    CaretakerPreferences copy = deepCloner.deepCopyPreference(original);

    assertThat(copy).isNotSameAs(original);
    assertThat(copy).usingRecursiveComparison().isEqualTo(original);
  }

  @Test
  @DisplayName("deepCopyPreference with null returns null")
  void deepCopyPreference_nullInput() {
    CaretakerPreferences copy = deepCloner.deepCopyPreference(null);

    assertThat(copy).isNull();
  }

  @Test
  @DisplayName("deepCopyPetProfile creates an independent copy")
  void deepCopyPetProfile_createsIndependentCopy() {
    PetProfile original = new PetProfile(
        new BigDecimal("12.5"), Sex.MALE, TriState.YES, TriState.YES,
        TriState.YES, TriState.UNKNOWN, TriState.YES,
        null, null, "VetClinic", "Loves walks", "Friendly"
    );

    PetProfile copy = deepCloner.deepCopyPetProfile(original);

    assertThat(copy).isNotSameAs(original);
    assertThat(copy).usingRecursiveComparison().isEqualTo(original);
    assertThat(copy.weightKg()).isEqualByComparingTo(new BigDecimal("12.5"));
    assertThat(copy.sex()).isEqualTo(Sex.MALE);
  }

  @Test
  @DisplayName("deepCopyPetProfile with null returns null")
  void deepCopyPetProfile_nullInput() {
    PetProfile copy = deepCloner.deepCopyPetProfile(null);

    assertThat(copy).isNull();
  }
}

