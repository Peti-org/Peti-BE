package com.peti.backend.dto;

import com.peti.backend.model.domain.Breed;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Breed type", defaultValue = "Dog")
  private String petType;
  @NotEmpty(message = "Breed name cannot be blank")
  @Schema(description = "Breed name", defaultValue = "Doberman")
  private String breedName;

  public static BreedDto from(Breed breed) {
    return new BreedDto(breed.getBreedId(), breed.getPetType(), breed.getBreedName());
  }
}
