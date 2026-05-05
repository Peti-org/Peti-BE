package com.peti.backend.dto.caretacker;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CaretakerDtoTest {

  @Test
  @DisplayName("convert maps Caretaker entity and preferences to CaretakerDto")
  void convert_mapsAllFields() {
    UUID caretakerId = UUID.randomUUID();
    User user = new User(UUID.randomUUID());
    user.setFirstName("Alice");
    user.setEmail("alice@test.com");

    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(caretakerId);
    caretaker.setUserReference(user);
    caretaker.setRating(5);

    CaretakerPreferences preferences = new CaretakerPreferences(null, null);

    CaretakerDto dto = CaretakerDto.convert(caretaker, preferences);

    assertThat(dto.getId()).isEqualTo(caretakerId);
    assertThat(dto.getName()).isEqualTo("Alice");
    assertThat(dto.getEmail()).isEqualTo("alice@test.com");
    assertThat(dto.getRating()).isEqualTo(5);
    assertThat(dto.getCaretakerPreference()).isSameAs(preferences);
  }
}

