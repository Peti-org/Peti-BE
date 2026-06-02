package com.peti.backend.dto.rrule;

import com.peti.backend.model.internal.ServiceType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Duration;
import java.time.LocalTime;

public record RequestRRuleDto(
    @NotBlank(message = "RRule must not be empty")
    String rrule,

    @NotNull(message = "Slot start time is required")
    LocalTime slotStartTime,

    @NotNull(message = "Slot duration is required")
    Duration slotDuration,

    String description,

    @NotNull(message = "Slot type is required")
    ServiceType slotType,

    @NotNull(message = "Pet capacity is required")
    @Positive(message = "Pet capacity must be positive")
    Integer petCapacity,

    @Positive(message = "People capacity must be positive")
    Integer peopleCapacity,

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

