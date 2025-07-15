package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
public class LoginUserDto {
  @Email(message = "Invalid email format")
  @NotBlank(message = "Email cannot be blank")
  @Schema(description = "User's email", defaultValue = "johnDoe@gmail.com")
  private String email;
  @NotEmpty(message = "Password cannot be blank")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  @Schema(description = "User's password (must be at least 8 characters long)", defaultValue = "StrongPassword123")
  private String password;
}
