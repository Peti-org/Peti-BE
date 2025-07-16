package com.peti.backend.dto.user;

import com.peti.backend.model.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.sql.Date;
import java.time.LocalDate;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
public class UpdateUserDto {

  @NotEmpty(message = "First name cannot be blank")
  @Schema(description = "User's name", defaultValue = "John")
  private String firstName;
  @NotEmpty(message = "Last name cannot be blank")
  @Schema(description = "User's surname", defaultValue = "Doe")
  private String lastName;
  @Past(message = "Birth date must be in the past.")
  @Schema(description = "User's birthday", defaultValue = "2000-01-01")
  private LocalDate birthDate;
  @NotNull(message = "City ID cannot be null")
  @Min(value = 1, message = "City ID must be a positive number")
  @Schema(description = "ID of the city where the user lives", defaultValue = "1")
  private Long cityId;

}
