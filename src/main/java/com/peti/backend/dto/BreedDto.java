package com.peti.backend.dto;

import com.peti.backend.model.Breed;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class BreedDto {
  private Integer id;
  private String name;
  private String species;

  public BreedDto(Integer id, String name, String species) {
    this.id = id;
    this.name = name;
    this.species = species;
  }

  public BreedDto(Breed breed) {
    this.id = breed.getBreedId();
    this.name = breed.getBreedName();
    this.species = breed.getPetType();
  }

  public Breed toBreed() {
    Breed breed = new Breed();
    breed.setBreedId(Math.toIntExact(id));
    breed.setBreedName(name);
    breed.setPetType(species);
    return breed;
  }
}