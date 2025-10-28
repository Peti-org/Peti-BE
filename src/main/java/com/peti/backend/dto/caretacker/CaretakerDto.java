package com.peti.backend.dto.caretacker;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class CaretakerDto {

  private UUID id;
  private String name;
  private String email;
  private String address;
  private Integer rating;

}
