package com.peti.backend.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after email change containing updated user and new auth tokens")
public record ChangeEmailResponse(
    UserDto user,
    AuthResponse auth
) {}

