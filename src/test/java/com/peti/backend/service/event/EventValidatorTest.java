package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
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
        List.of(pets.getFirst()), List.of(PET_A, PET_B), USER_ID))
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

  // ---------- validateTimeOrder ----------

  @Test
  @DisplayName("validateTimeOrder - valid ordering passes")
  void validateTimeOrder_valid() {
    assertThatCode(() -> validator.validateTimeOrder(
        LocalDateTime.of(2026, 5, 2, 10, 0),
        LocalDateTime.of(2026, 5, 2, 11, 0)))
        .doesNotThrowAnyException();
  }

  @Test
  @DisplayName("validateTimeOrder - from >= to throws BadRequestException")
  void validateTimeOrder_fromAfterTo() {
    assertThatThrownBy(() -> validator.validateTimeOrder(
        LocalDateTime.of(2026, 5, 2, 11, 0),
        LocalDateTime.of(2026, 5, 2, 10, 0)))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("strictly before");
  }

  @Test
  @DisplayName("validateTimeOrder - from equals to throws BadRequestException")
  void validateTimeOrder_equal() {
    LocalDateTime same = LocalDateTime.of(2026, 5, 2, 10, 0);
    assertThatThrownBy(() -> validator.validateTimeOrder(same, same))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("strictly before");
  }
}

