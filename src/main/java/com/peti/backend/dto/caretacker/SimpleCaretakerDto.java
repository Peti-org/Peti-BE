package com.peti.backend.dto.caretacker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class SimpleCaretakerDto {

  private final UUID id;
  private final String firstName;
  private final String lastName;
  private final Integer rating;

}
