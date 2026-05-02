package com.peti.backend.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to transition an order to a new status")
public record RequestOrderTransitionDto(
    @Schema(description = "Optional comment explaining the transition")
    @Size(max = 500, message = "Comment must not exceed 500 characters")
    String comment) {
}

