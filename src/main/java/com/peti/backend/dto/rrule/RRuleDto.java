package com.peti.backend.dto.rrule;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.internal.ServiceType;
import java.time.Duration;
import java.time.LocalDate;
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

  public static RRuleDto convert(CaretakerRRule rrule) {
    LocalDate today = LocalDate.now();
    LocalDateTime dtstart = rrule.getSlotStartTime() != null
        ? today.atTime(rrule.getSlotStartTime()) : null;
    Duration duration = rrule.getSlotDuration();
    LocalDateTime dtend = (dtstart != null && duration != null)
        ? dtstart.plus(duration) : null;

    return new RRuleDto(
        rrule.getRruleId(),
        rrule.getRrule(),
        dtstart,
        dtend,
        rrule.getDescription(),
        ServiceType.fromName(rrule.getSlotType()),
        rrule.getCapacity(),
        rrule.getIntervalMinutes(),
        rrule.getIsEnabled(),
        rrule.getIsSchedule(),
        rrule.getIsBusy(),
        rrule.getPriority());
  }
}

