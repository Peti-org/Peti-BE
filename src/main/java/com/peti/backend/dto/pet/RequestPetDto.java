package com.peti.backend.dto.pet;

import com.peti.backend.model.Breed;
import com.peti.backend.model.Pet;
import com.peti.backend.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
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
  @Schema(description = "Pet name", defaultValue = "Sirko")
  private String name;
  @Past(message = "Birth date must be in the past.")
  @Schema(description = "Pet birth date", defaultValue = "2020-01-01")
  private LocalDate dateOfBirth;
  @NotNull(message = "Breed ID cannot be null")
  @Min(value = 1, message = "Breed ID must be a positive number")
  @Schema(description = "Pet breed id", defaultValue = "1")
  private Integer breedId;
}
