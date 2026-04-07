package com.peti.backend.dto.rrule;

import com.peti.backend.model.internal.ServiceType;
import java.time.LocalDateTime;
import java.util.UUID;

public record RRuleDto(
    UUID rruleId,
    String rrule,
    LocalDateTime dtstart,
    LocalDateTime dtend,
    String description,
    ServiceType slotType,
    Integer capacity,
    Integer intervalMinutes,
    Boolean isEnabled,
    Boolean isSchedule,
    Boolean isBusy,
    Integer priority
) {
}

