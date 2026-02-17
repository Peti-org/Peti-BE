package com.peti.backend.dto.rrule;

import static org.assertj.core.api.Assertions.assertThat;
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

public class RequestRRuleDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  public static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidRRuleDto() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(30),
        "Test description",
        "walk",
        5,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void testRRuleBlank() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        5,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("RRule must not be empty")));
  }

  @Test
  public void testDtstartNull() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        null,
        null,
        "Test",
        "walk",
        5,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Start date/time is required")));
  }

  @Test
  public void testSlotTypeBlank() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "",
        5,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Slot type is required")));
  }

  @Test
  public void testCapacityNull() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        null,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Capacity is required")));
  }

  @Test
  public void testCapacityZero() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        0,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Capacity must be positive")));
  }

  @Test
  public void testCapacityNegative() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        -1,
        30
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
  }

  @Test
  public void testIntervalMinutesNull() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        5,
        null
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Interval minutes is required")));
  }

  @Test
  public void testIntervalMinutesZero() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "FREQ=DAILY",
        LocalDateTime.now(),
        null,
        "Test",
        "walk",
        5,
        0
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Interval must be at least 1 minute")));
  }

  @Test
  public void testMultipleViolations() {
    RequestRRuleDto dto = new RequestRRuleDto(
        "",
        null,
        null,
        "Test",
        "",
        -1,
        0
    );

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertThat(violations).hasSize(5);
  }
}

