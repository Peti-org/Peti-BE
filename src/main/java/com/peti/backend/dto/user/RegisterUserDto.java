package com.peti.backend.dto.user;

import com.peti.backend.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.time.LocalDate;

@Getter
public class RegisterUserDto {

  @Email(message = "Invalid email format")
  @NotBlank(message = "Email cannot be blank")
  @Schema(description = "User's email", defaultValue = "johnDoe@gmail.com")
  private String email;
  @NotEmpty(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Schema(description = "User's password (must be at least 8 characters long)", defaultValue = "StrongPassword123")
  private String password;
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

  public User toUser(PasswordEncoder passwordEncoder){
    User user = new User();
    user.setFirstName(firstName);
    user.setLastName(lastName);
    user.setEmail(email);
    user.setPassword(passwordEncoder.encode(password));
    user.setBirthday(Date.valueOf(birthDate));
    user.setUserDataFolder("default"); //TODO: mock folder as we don't have such a feature right now
    user.setUserIsDeleted(false);
    return user;
  }
}
