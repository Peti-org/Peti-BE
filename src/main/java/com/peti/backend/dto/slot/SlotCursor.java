package com.peti.backend.dto.slot;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record SlotCursor(
    @NotNull(message = "Rating is required")
    @Min(message = "Rating must be at least 1", value = 0)
    @Max(message = "Rating must be at most 100", value = 100)
    Integer userRating,
    @NotNull(message = "Created at time is required")
    LocalDateTime createdAt,
    @NotNull(message = "Limit bounds of results is required")
    Integer limit) {

}
