package com.peti.backend.dto.rrule;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record RequestRRuleDto(
    @NotBlank(message = "RRule must not be empty")
    String rrule,

    @NotNull(message = "Start date/time is required")
    LocalDateTime dtstart,

    LocalDateTime dtend,

    String description,

    @NotBlank(message = "Slot type is required")
    String slotType
) {
}

