package com.peti.backend.dto;

import com.peti.backend.model.Breed;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class BreedDto {

  private Integer id;
  @NotEmpty(message = "Breed type cannot be blank")
  private String petType;
  @NotEmpty(message = "Breed name cannot be blank")
  private String breedName;

  public static BreedDto from(Breed breed) {
    return new BreedDto(breed.getBreedId(), breed.getPetType(), breed.getBreedName());
  }
}
