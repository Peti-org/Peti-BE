package com.peti.backend.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RequestReactionDto(
    @NotBlank(message = "Target type must not be blank")
    String targetType,

    @NotNull(message = "Target ID must not be null")
    UUID targetId
) {
}

