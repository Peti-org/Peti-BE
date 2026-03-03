package com.peti.backend.dto.rrule;

import com.peti.backend.model.internal.ServiceType;
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

    @NotNull(message = "Slot type is required")
    ServiceType slotType,

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    Integer capacity,

    @NotNull(message = "Interval minutes is required")
    @Min(value = 1, message = "Interval must be at least 1 minute")
    Integer intervalMinutes,

    @NotNull(message = "IsEnabled is required")
    Boolean isEnabled,

    @NotNull(message = "IsSchedule is required")
    Boolean isSchedule,

    @NotNull(message = "IsBusy is required")
    Boolean isBusy,

    @NotNull(message = "Priority is required")
    @Min(value = 0, message = "Priority must be at least 0")
    Integer priority
) {
}

