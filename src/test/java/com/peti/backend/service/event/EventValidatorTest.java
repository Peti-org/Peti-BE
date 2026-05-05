package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.exception.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventValidatorTest {

  private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final UUID PET_A = UUID.fromString("aaaaaaaa-1111-1111-1111-111111111111");
  private static final UUID PET_B = UUID.fromString("bbbbbbbb-2222-2222-2222-222222222222");

  private final EventValidator validator = new EventValidator();

  // ---------- validatePetOwnership ----------

  @Test
  @DisplayName("validatePetOwnership - all pets owned, no exception")
  void validatePetOwnership_allOwned() {
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    assertThatCode(() -> validator.validatePetOwnership(pets, List.of(PET_A, PET_B), USER_ID))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("validatePetOwnership - size mismatch throws BadRequestException")
  void validatePetOwnership_sizeMismatch() {
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    assertThatThrownBy(() -> validator.validatePetOwnership(
        List.of(pets.get(0)), List.of(PET_A, PET_B), USER_ID))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("do not belong");
  }

  @Test
  @DisplayName("validatePetOwnership - wrong owner throws BadRequestException")
  void validatePetOwnership_wrongOwner() {
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    UUID otherUser = UUID.randomUUID();
    assertThatThrownBy(() -> validator.validatePetOwnership(pets, List.of(PET_A, PET_B), otherUser))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("do not belong");
  }

  // ---------- validateTimeWindow ----------

  @Test
  @DisplayName("validateTimeWindow - valid window inside range passes")
  void validateTimeWindow_valid() {
    CaretakerRRule rrule = enabledRule();

    assertThatCode(() -> validator.validateTimeWindow(rrule,
        LocalDateTime.of(2026, 5, 2, 10, 0),
        LocalDateTime.of(2026, 5, 2, 11, 0)))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("validateTimeWindow - from >= to throws BadRequestException")
  void validateTimeWindow_fromAfterTo() {
    CaretakerRRule rrule = enabledRule();

    assertThatThrownBy(() -> validator.validateTimeWindow(rrule,
        LocalDateTime.of(2026, 5, 2, 11, 0),
        LocalDateTime.of(2026, 5, 2, 10, 0)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("strictly before");
  }

  @Test
  @DisplayName("validateTimeWindow - from before rrule.dtstart throws")
  void validateTimeWindow_beforeDtstart() {
    CaretakerRRule rrule = enabledRule();

    assertThatThrownBy(() -> validator.validateTimeWindow(rrule,
        LocalDateTime.of(2025, 12, 31, 10, 0),
        LocalDateTime.of(2025, 12, 31, 11, 0)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("dtstart");
  }

  @Test
  @DisplayName("validateTimeWindow - to after rrule.dtend throws")
  void validateTimeWindow_afterDtend() {
    CaretakerRRule rrule = enabledRule();

    assertThatThrownBy(() -> validator.validateTimeWindow(rrule,
        LocalDateTime.of(2027, 1, 1, 10, 0),
        LocalDateTime.of(2027, 1, 1, 11, 0)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("dtend");
  }

  @Test
  @DisplayName("validateTimeWindow - disabled rrule throws")
  void validateTimeWindow_disabled() {
    CaretakerRRule rrule = enabledRule();
    rrule.setIsEnabled(false);

    assertThatThrownBy(() -> validator.validateTimeWindow(rrule,
        LocalDateTime.of(2026, 5, 2, 10, 0),
        LocalDateTime.of(2026, 5, 2, 11, 0)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("disabled");
  }

  private CaretakerRRule enabledRule() {
    return ResourceLoader.loadResource("rrule-for-event-entity.json", CaretakerRRule.class);
  }
}
