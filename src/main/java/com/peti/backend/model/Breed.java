package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Breed {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "breed_id", nullable = false)
  private int breedId;
  @Basic
  @Column(name = "pet_type", nullable = false, length = 50)
  private String petType;
  @Basic
  @Column(name = "breed_name", nullable = false, length = 50)
  private String breedName;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Breed breed = (Breed) o;

    if (breedId != breed.breedId)
      return false;
    if (petType != null ? !petType.equals(breed.petType) : breed.petType != null)
      return false;
    if (breedName != null ? !breedName.equals(breed.breedName) : breed.breedName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = breedId;
    result = 31 * result + (petType != null ? petType.hashCode() : 0);
    result = 31 * result + (breedName != null ? breedName.hashCode() : 0);
    return result;
  }
}
