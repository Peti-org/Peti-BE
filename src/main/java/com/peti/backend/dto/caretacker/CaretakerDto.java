package com.peti.backend.dto.caretacker;

import java.util.UUID;
import com.peti.backend.model.domain.Caretaker;
import jakarta.validation.Valid;
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
  @Valid private CaretakerPreferences caretakerPreference;

  public static CaretakerDto convert(Caretaker caretaker, CaretakerPreferences caretakerPreferences) {
    CaretakerDto caretakerDto = new CaretakerDto();
    caretakerDto.setId(caretaker.getCaretakerId());
    caretakerDto.setName(caretaker.getUserReference().getFirstName());
    caretakerDto.setEmail(caretaker.getUserReference().getEmail());
    caretakerDto.setRating(caretaker.getRating());
    caretakerDto.setCaretakerPreference(caretakerPreferences);
    return caretakerDto;
  }


}
