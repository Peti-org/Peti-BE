package com.peti.backend.dto.event;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RequestEventDtoValidationTest {

  private static Validator validator;

  @BeforeAll
  public static void setUpValidator() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @Test
  public void testValidEventDto() {
    RequestEventDto dto = new RequestEventDto(
        Arrays.asList(UUID.randomUUID(), UUID.randomUUID()),
        Arrays.asList(UUID.randomUUID())
    );

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertTrue(violations.isEmpty());
  }

  @Test
  public void testSlotsListEmpty() {
    RequestEventDto dto = new RequestEventDto(
        Collections.emptyList(),
        Arrays.asList(UUID.randomUUID())
    );

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Slots list must not be empty")));
  }

  @Test
  public void testPetsListEmpty() {
    RequestEventDto dto = new RequestEventDto(
        Arrays.asList(UUID.randomUUID()),
        Collections.emptyList()
    );

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertFalse(violations.isEmpty());
    assertTrue(violations.stream()
        .anyMatch(v -> v.getMessage().contains("Pets list must not be empty")));
  }

  @Test
  public void testBothListsEmpty() {
    RequestEventDto dto = new RequestEventDto(
        Collections.emptyList(),
        Collections.emptyList()
    );

    Set<ConstraintViolation<RequestEventDto>> violations = validator.validate(dto);
    assertTrue(violations.size() >= 2);
  }
}

