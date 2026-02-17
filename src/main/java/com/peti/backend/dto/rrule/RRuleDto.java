package com.peti.backend.dto.rrule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record RRuleDto(
    UUID rruleId,
    String rrule,
    LocalDateTime dtstart,
    LocalDateTime dtend,
    String description,
    String slotType,
    Integer capacity,
    Integer intervalMinutes
) {
}

