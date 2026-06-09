package com.peti.backend.dto.rrule;

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

class RRuleDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  static void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  private static RequestRRuleDto valid(String rrule, LocalTime start, Duration duration,
      String description) {
    return new RequestRRuleDto(rrule, start, duration, description,
        ServiceType.WALKING, 3, 4, true, false,  0);
  }

  @Test
  void testCreateRRuleDto_Valid() {
    RequestRRuleDto dto = valid("FREQ=WEEKLY;BYDAY=MO,WE,FR",
        LocalTime.of(9, 0), Duration.ofHours(9), "Mon/Wed/Fri availability");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
  }

  @Test
  void testCreateRRuleDto_NullRRule() {
    RequestRRuleDto dto = valid(null, LocalTime.of(9, 0), Duration.ofHours(9), "Description");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testCreateRRuleDto_EmptyRRule() {
    RequestRRuleDto dto = valid("", LocalTime.of(9, 0), Duration.ofHours(9), "Description");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }

  @Test
  void testCreateRRuleDto_NullSlotStartTime() {
    RequestRRuleDto dto = valid("FREQ=DAILY", null, Duration.ofHours(9), "Description");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot start time is required")));
  }

  @Test
  void testCreateRRuleDto_NullSlotDuration() {
    RequestRRuleDto dto = valid("FREQ=DAILY", LocalTime.of(9, 0), null, "Description");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Slot duration is required")));
  }

  @Test
  void testCreateRRuleDto_NullDescriptionAllowed() {
    RequestRRuleDto dto = valid("FREQ=WEEKLY;BYDAY=SA,SU", LocalTime.of(9, 0),
        Duration.ofHours(9), null);

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty(), "Null description should be allowed");
  }

  @Test
  void testUpdateRRuleDto_Valid() {
    RequestRRuleDto dto = valid("FREQ=WEEKLY;BYDAY=TU,TH",
        LocalTime.of(10, 0), Duration.ofHours(7), "Updated availability");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty(), "Valid DTO should have no violations");
  }

  @Test
  void testUpdateRRuleDto_BlankRRule() {
    RequestRRuleDto dto = valid("   ", LocalTime.of(9, 0), Duration.ofHours(9), "Description");

    Set<ConstraintViolation<RequestRRuleDto>> violations = validator.validate(dto);
    assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("must not be empty")));
  }
}

