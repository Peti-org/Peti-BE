package com.peti.backend.dto.rrule;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.internal.ServiceType;
import java.time.Duration;
import java.time.LocalTime;
import java.util.UUID;

public record RRuleDto(
    UUID rruleId,
    String rrule,
    LocalTime slotStartTime,
    Duration slotDuration,
    String description,
    ServiceType slotType,
    Integer petCapacity,
    Integer peopleCapacity,
    Boolean isEnabled,
    Boolean isSchedule,
    Boolean isBusy,
    Integer priority
) {

  public static RRuleDto convert(CaretakerRRule rrule) {
    return new RRuleDto(
        rrule.getRruleId(),
        rrule.getRrule(),
        rrule.getSlotStartTime(),
        rrule.getSlotDuration(),
        rrule.getDescription(),
        ServiceType.fromName(rrule.getSlotType()),
        rrule.getPetCapacity(),
        rrule.getPeopleCapacity(),
        rrule.getIsEnabled(),
        rrule.getIsSchedule(),
        rrule.getIsBusy(),
        rrule.getPriority());
  }
}

