package com.peti.backend.dto.pet;

import com.peti.backend.dto.BreedDto;
import com.peti.backend.model.domain.Pet;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PetDto {

  private UUID petId;
  private String name;
  private LocalDate dateOfBirth;
  private BreedDto breed;

  public static PetDto from(Pet pet) {
    return new PetDto(pet.getPetId(), pet.getName(), pet.getBirthday().toLocalDate(), BreedDto.from(pet.getBreed()));
  }
}
