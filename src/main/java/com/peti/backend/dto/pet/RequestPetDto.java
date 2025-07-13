package com.peti.backend.dto.pet;

import com.peti.backend.model.Breed;
import com.peti.backend.model.Pet;
import com.peti.backend.model.User;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import java.sql.Date;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestPetDto {

  @NotEmpty(message = "Pet name cannot be blank")
  private String name;
  @Past(message = "Birth date must be in the past.")
  private LocalDate dateOfBirth;
  @NotNull(message = "Breed ID cannot be null")
  @Min(value = 1, message = "Breed ID must be a positive number")
  private Integer breedId;
}
