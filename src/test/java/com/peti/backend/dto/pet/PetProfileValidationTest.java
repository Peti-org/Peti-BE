package com.peti.backend.dto.pet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.peti.backend.dto.pet.PetProfile.Sex;
import com.peti.backend.dto.pet.PetProfile.TriState;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PetProfileValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void validProfile_noViolations() {
    PetProfile profile = new PetProfile(
        new BigDecimal("12.5"), Sex.MALE, TriState.YES, TriState.YES,
        TriState.YES, TriState.UNKNOWN, TriState.YES,
        null, null, null, null, "Good boy"
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertTrue(violations.isEmpty());
  }

  @Test
  void nullWeight_isValid() {
    PetProfile profile = new PetProfile(
        null, Sex.FEMALE, TriState.NO, TriState.UNKNOWN,
        TriState.UNKNOWN, TriState.UNKNOWN, TriState.UNKNOWN,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertTrue(violations.isEmpty());
  }

  @Test
  void nullSex_isInvalid() {
    PetProfile profile = new PetProfile(
        new BigDecimal("5.0"), null, TriState.YES, TriState.YES,
        TriState.YES, TriState.YES, TriState.YES,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }

  @Test
  void nullSterilized_isInvalid() {
    PetProfile profile = new PetProfile(
        null, Sex.MALE, null, TriState.YES,
        TriState.YES, TriState.YES, TriState.YES,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }

  @Test
  void nullVaccinated_isInvalid() {
    PetProfile profile = new PetProfile(
        null, Sex.MALE, TriState.YES, null,
        TriState.YES, TriState.YES, TriState.YES,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }

  @Test
  void allUnknown_isValid() {
    PetProfile profile = new PetProfile(
        null, Sex.UNKNOWN, TriState.UNKNOWN, TriState.UNKNOWN,
        TriState.UNKNOWN, TriState.UNKNOWN, TriState.UNKNOWN,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertTrue(violations.isEmpty());
  }

  @Test
  void allFieldsFilled_isValid() {
    PetProfile profile = new PetProfile(
        new BigDecimal("30.0"), Sex.FEMALE, TriState.YES, TriState.YES,
        TriState.NO, TriState.YES, TriState.NO,
        "Chicken allergy", "Apoquel 16mg daily", "VetClinic +380441112233",
        "Needs muzzle outdoors", "Large friendly lab"
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertTrue(violations.isEmpty());
  }

  @Test
  void zeroWeight_isInvalid() {
    PetProfile profile = new PetProfile(
        new BigDecimal("0.00"), Sex.MALE, TriState.YES, TriState.YES,
        TriState.YES, TriState.YES, TriState.YES,
        null, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }

  @Test
  void descriptionTooLong_isInvalid() {
    String longText = "A".repeat(2001);
    PetProfile profile = new PetProfile(
        null, Sex.MALE, TriState.UNKNOWN, TriState.UNKNOWN,
        TriState.UNKNOWN, TriState.UNKNOWN, TriState.UNKNOWN,
        null, null, null, null, longText
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }

  @Test
  void allergiesTooLong_isInvalid() {
    String longText = "A".repeat(1001);
    PetProfile profile = new PetProfile(
        null, Sex.MALE, TriState.UNKNOWN, TriState.UNKNOWN,
        TriState.UNKNOWN, TriState.UNKNOWN, TriState.UNKNOWN,
        longText, null, null, null, null
    );

    Set<ConstraintViolation<PetProfile>> violations = validator.validate(profile);
    assertFalse(violations.isEmpty());
  }
}

