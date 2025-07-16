package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdatePasswordDto {

  @NotEmpty(message = "New password cannot be blank")
  @Size(min = 8, message = "New password must be at least 8 characters long")
  @Schema(description = "User's new password (must be at least 8 characters long)", defaultValue = "StrongPassword123")
  private String newPassword;
  @NotEmpty(message = "Old password cannot be blank")
  @Size(min = 8, message = "Old password must be at least 8 characters long")
  @Schema(description = "User's old password (must be at least 8 characters long)", defaultValue = "StrongPassword123")
  private String oldPassword;
}
