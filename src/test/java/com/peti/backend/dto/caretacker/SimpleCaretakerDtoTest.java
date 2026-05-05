package com.peti.backend.dto.caretacker;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SimpleCaretakerDtoTest {

  @Test
  @DisplayName("convert maps Caretaker entity to SimpleCaretakerDto")
  void convert_mapsAllFields() {
    UUID caretakerId = UUID.randomUUID();
    User user = new User(UUID.randomUUID());
    user.setFirstName("John");
    user.setLastName("Doe");

    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(caretakerId);
    caretaker.setUserReference(user);
    caretaker.setRating(4);

    SimpleCaretakerDto dto = SimpleCaretakerDto.convert(caretaker);

    assertThat(dto.id()).isEqualTo(caretakerId);
    assertThat(dto.firstName()).isEqualTo("John");
    assertThat(dto.lastName()).isEqualTo("Doe");
    assertThat(dto.rating()).isEqualTo(4);
  }
}

