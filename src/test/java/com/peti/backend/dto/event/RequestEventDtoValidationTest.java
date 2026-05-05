package com.peti.backend.dto.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RequestEventDtoValidationTest {

  private static Validator validator;

  private static final UUID RRULE_ID = UUID.randomUUID();
  private static final LocalDateTime FROM = LocalDateTime.of(2026, 5, 1, 10, 0);
  private static final LocalDateTime TO = LocalDateTime.of(2026, 5, 1, 12, 0);

  @BeforeAll
  static void setUpValidator() {
    try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
      validator = factory.getValidator();
    }
  }

  @Test
  @DisplayName("Valid DTO produces no violations")
  void validDto_noViolations() {
    RequestEventDto dto = new RequestEventDto(
        RRULE_ID, FROM, TO, List.of(UUID.randomUUID()));

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty());
  }

  @Test
  @DisplayName("Missing rruleId triggers @NotNull")
  void missingRRuleId() {
    RequestEventDto dto = new RequestEventDto(null, FROM, TO, List.of(UUID.randomUUID()));

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("rruleId must not be null")));
  }

  @Test
  @DisplayName("Missing datetimeFrom triggers @NotNull")
  void missingDatetimeFrom() {
    RequestEventDto dto = new RequestEventDto(RRULE_ID, null, TO, List.of(UUID.randomUUID()));

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("datetimeFrom must not be null")));
  }

  @Test
  @DisplayName("Empty pets list triggers @NotEmpty")
  void emptyPetsList() {
    RequestEventDto dto = new RequestEventDto(RRULE_ID, FROM, TO, Collections.emptyList());

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Pets list must not be empty")));
  }
}

