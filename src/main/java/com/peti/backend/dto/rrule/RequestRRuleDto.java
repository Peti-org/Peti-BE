package com.peti.backend.dto.rrule;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record RequestRRuleDto(
    @NotBlank(message = "RRule must not be empty")
    String rrule,

    @NotNull(message = "Start date/time is required")
    LocalDateTime dtstart,

    LocalDateTime dtend,

    String description,

    @NotBlank(message = "Slot type is required")
    String slotType,

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    Integer capacity,

    @NotNull(message = "Interval minutes is required")
    @Min(value = 1, message = "Interval must be at least 1 minute")
    Integer intervalMinutes
) {
}

