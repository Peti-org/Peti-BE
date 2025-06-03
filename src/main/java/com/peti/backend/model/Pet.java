package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Date;
import java.util.UUID;

@Entity
@Table(name = "pet", schema = "peti", catalog = "peti")
public class Pet {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "pet_id", nullable = false)
  @Getter
  @Setter
  private UUID petId;
  @Basic
  @Column(name = "name", nullable = false, length = 50)
  private String name;
  @Basic
  @Column(name = "birthday", nullable = false)
  private Date birthday;
  @Basic
  @Column(name = "context", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object context;
  @Basic
  @Column(name = "pet_data_folder", nullable = false, length = 50)
  private String petDataFolder;
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User userByUserId;
  @ManyToOne
  @JoinColumn(name = "breed_id", referencedColumnName = "breed_id", nullable = false)
  private Breed breedByBreedId;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getBirthday() {
    return birthday;
  }

  public void setBirthday(Date birthday) {
    this.birthday = birthday;
  }

  public Object getContext() {
    return context;
  }

  public void setContext(Object context) {
    this.context = context;
  }

  public String getPetDataFolder() {
    return petDataFolder;
  }

  public void setPetDataFolder(String petDataFolder) {
    this.petDataFolder = petDataFolder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Pet pet = (Pet) o;

    if (petId != null ? !petId.equals(pet.petId) : pet.petId != null)
      return false;
    if (name != null ? !name.equals(pet.name) : pet.name != null)
      return false;
    if (birthday != null ? !birthday.equals(pet.birthday) : pet.birthday != null)
      return false;
    if (context != null ? !context.equals(pet.context) : pet.context != null)
      return false;
    if (petDataFolder != null ? !petDataFolder.equals(pet.petDataFolder) : pet.petDataFolder != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = petId != null ? petId.hashCode() : 0;
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (birthday != null ? birthday.hashCode() : 0);
    result = 31 * result + (context != null ? context.hashCode() : 0);
    result = 31 * result + (petDataFolder != null ? petDataFolder.hashCode() : 0);
    return result;
  }

  public User getUserByUserId() {
    return userByUserId;
  }

  public void setUserByUserId(User userByUserId) {
    this.userByUserId = userByUserId;
  }

  public Breed getBreedByBreedId() {
    return breedByBreedId;
  }

  public void setBreedByBreedId(Breed breedByBreedId) {
    this.breedByBreedId = breedByBreedId;
  }
}
