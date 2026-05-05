package com.peti.backend.dto.caretacker;

import com.peti.backend.model.domain.Caretaker;
import java.util.UUID;

public record SimpleCaretakerDto(UUID id, String firstName, String lastName, Integer rating) {

  public static SimpleCaretakerDto convert(Caretaker caretaker) {
    return new SimpleCaretakerDto(
        caretaker.getCaretakerId(),
        caretaker.getUserReference().getFirstName(),
        caretaker.getUserReference().getLastName(),
        caretaker.getRating()
    );
  }

}
