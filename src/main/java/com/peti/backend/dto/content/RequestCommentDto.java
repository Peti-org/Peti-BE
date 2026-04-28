package com.peti.backend.dto.content;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record RequestCommentDto(
    @NotBlank(message = "Content must not be blank")
    @Size(max = 1000, message = "Comment must not exceed 1000 characters")
    String content,

    @NotBlank(message = "Target type must not be blank")
    String targetType,

    @NotNull(message = "Target ID must not be null")
    UUID targetId,

    UUID parentCommentId
) {
}

