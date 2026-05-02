package com.peti.backend.dto.pet;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.pet.PetProfile.Sex;
import com.peti.backend.dto.pet.PetProfile.TriState;
import com.peti.backend.model.domain.Breed;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.domain.User;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PetDtoTest {

  @Test
  @DisplayName("convert maps Pet entity and PetProfile to PetDto")
  void convert_mapsAllFields() {
    Breed breed = new Breed(1);
    breed.setPetType("Dog");
    breed.setBreedName("Labrador");

    PetProfile profile = new PetProfile(
        new BigDecimal("25.0"), Sex.FEMALE, TriState.YES, TriState.YES,
        TriState.YES, TriState.YES, TriState.YES,
        null, null, "Vet", "Notes", "Description"
    );

    UUID petId = UUID.randomUUID();
    Pet pet = new Pet();
    pet.setPetId(petId);
    pet.setName("Luna");
    pet.setBirthday(Date.valueOf("2021-06-15"));
    pet.setBreed(breed);
    pet.setPetOwner(new User(UUID.randomUUID()));
    pet.setContext(profile);

    PetDto dto = PetDto.convert(pet, profile);

    assertThat(dto.getPetId()).isEqualTo(petId);
    assertThat(dto.getName()).isEqualTo("Luna");
    assertThat(dto.getDateOfBirth()).isEqualTo(LocalDate.of(2021, 6, 15));
    assertThat(dto.getBreed().getBreedName()).isEqualTo("Labrador");
    assertThat(dto.getProfile()).isSameAs(profile);
  }
}

