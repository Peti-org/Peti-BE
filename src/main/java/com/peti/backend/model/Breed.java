package com.peti.backend.model;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@Table(name = "breed", schema = "peti", catalog = "peti")
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

  public Breed(int breedId) {
    this.breedId = breedId;
  }
}
