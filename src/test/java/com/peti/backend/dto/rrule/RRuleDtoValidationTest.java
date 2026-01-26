package com.peti.backend.dto.rrule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RRuleDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  void testCreateRRuleDto_Valid() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=WEEKLY;BYDAY=MO,WE,FR",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Mon/Wed/Fri availability",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
  }

  @Test
  void testCreateRRuleDto_NullRRule() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        null,
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testCreateRRuleDto_EmptyRRule() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testCreateRRuleDto_NullDtstart() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        null,
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertEquals(1, violations.size());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("is required")));
  }

  @Test
  void testCreateRRuleDto_NullDtendAllowed() {
    // Given - dtend is nullable
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        null,
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertTrue(violations.isEmpty(), "Null dtend should be allowed");
  }

  @Test
  void testCreateRRuleDto_NullDescriptionAllowed() {
    // Given - description is nullable
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=WEEKLY;BYDAY=SA,SU",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        null,
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertTrue(violations.isEmpty(), "Null description should be allowed");
  }

  @Test
  void testUpdateRRuleDto_Valid() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=WEEKLY;BYDAY=TU,TH",
        LocalDateTime.of(2026, 2, 1, 10, 0),
        LocalDateTime.of(2026, 11, 30, 17, 0),
        "Updated availability",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
  }

  @Test
  void testUpdateRRuleDto_NullRRule() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        null,
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testUpdateRRuleDto_EmptyRRule() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "   ",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testUpdateRRuleDto_NullDtstart() {
    // Given
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        null,
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Description",
        "walk"
    );

    // When
    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);

    // Then
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("is required")));
  }

  @Test
  void testRRuleDto_RecordImmutability() {
    // Given
    RRuleDto dto = new RRuleDto(
        java.util.UUID.randomUUID(),
        "FREQ=DAILY",
        LocalDateTime.of(2026, 1, 1, 9, 0),
        LocalDateTime.of(2026, 12, 31, 18, 0),
        "Test", "walk");

    // Then - Records are immutable, no setters available
    assertFalse(dto.rrule().isEmpty());
    assertEquals("FREQ=DAILY", dto.rrule());
  }
}

