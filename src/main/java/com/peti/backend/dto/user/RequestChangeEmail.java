package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to change the authenticated user's email")
public record RequestChangeEmail(
    @NotBlank(message = "New email must not be blank")
    @Email(message = "Invalid email format")
    @Schema(description = "The new email address", example = "newemail@example.com")
    String newEmail,

    @NotBlank(message = "Current password must not be blank")
    @Schema(description = "Current password for verification", example = "MyCurrentPass123")
    String currentPassword
) {}

