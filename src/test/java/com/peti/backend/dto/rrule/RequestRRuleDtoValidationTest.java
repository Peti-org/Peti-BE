package com.peti.backend.dto.rrule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.peti.backend.model.internal.ServiceType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.Duration;
import java.time.LocalTime;
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

  private static RequestRRuleDto build(String rrule, LocalTime start, Duration duration,
      ServiceType type, Integer petCap, Integer peopleCap,
      Boolean enabled, Boolean schedule, Boolean busy, Integer priority) {
    return new RequestRRuleDto(rrule, start, duration, "Test", type, petCap, peopleCap,
        enabled, schedule, busy, priority);
  }

  @Test
  public void testValidRRuleDto() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, 5, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void testPeopleCapacityNullAllowed() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, 5, null, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty(), "people capacity is optional");
  }

  @Test
  public void testPeopleCapacityZero_Invalid() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, 5, 0, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("People capacity must be positive")));
  }

  @Test
  public void testRRuleBlank() {
    RequestRRuleDto dto = build("", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, 5, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("RRule must not be empty")));
  }

  @Test
  public void testSlotStartTimeNull() {
    RequestRRuleDto dto = build("FREQ=DAILY", null, Duration.ofHours(8),
        ServiceType.WALKING, 5, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Slot start time is required")));
  }

  @Test
  public void testSlotDurationNull() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), null,
        ServiceType.WALKING, 5, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Slot duration is required")));
  }

  @Test
  public void testSlotTypeNull() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        null, 5, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Slot type is required")));
  }

  @Test
  public void testPetCapacityNull() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, null, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Pet capacity is required")));
  }

  @Test
  public void testPetCapacityZero() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, 0, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Pet capacity must be positive")));
  }

  @Test
  public void testPetCapacityNegative() {
    RequestRRuleDto dto = build("FREQ=DAILY", LocalTime.of(9, 0), Duration.ofHours(8),
        ServiceType.WALKING, -1, 4, true, false, false, 0);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
  }

  @Test
  public void testMultipleViolations() {
    RequestRRuleDto dto = build("", null, null, ServiceType.WALKING, -1, -1,
        null, null, null, -1);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertThat(violations).hasSizeGreaterThanOrEqualTo(8);
  }
}

