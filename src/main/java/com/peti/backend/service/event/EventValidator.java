package com.peti.backend.service.event;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.exception.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validates inputs for event creation: pet ownership and time window vs RRule range.
 */
@Component
public class EventValidator {

  /**
   * Validates that all pets belong to the given user.
   *
   * @param pets   pre-fetched pets
   * @param petIds requested pet IDs
   * @param userId the owner
   * @throws BadRequestException if any pet is missing or not owned by the user
   */
  public void validatePetOwnership(List<Pet> pets, List<UUID> petIds, UUID userId) {
    if (pets.size() != petIds.size()) {
      throw new BadRequestException("Some pets do not belong to the user");
    }
    boolean allOwned = pets.stream()
        .allMatch(p -> p.getPetOwner() != null && userId.equals(p.getPetOwner().getUserId()));
    if (!allOwned) {
      throw new BadRequestException("Some pets do not belong to the user");
    }
  }

  /**
   * Validates the requested time window against the RRule date range and ordering.
   */
  public void validateTimeWindow(CaretakerRRule rrule,
      LocalDateTime from, LocalDateTime to) {
    if (!from.isBefore(to)) {
      throw new BadRequestException("datetimeFrom must be strictly before datetimeTo");
    }
    if (rrule.getDtstart() != null && from.isBefore(rrule.getDtstart())) {
      throw new BadRequestException("Event starts before RRule dtstart");
    }
    if (rrule.getDtend() != null && to.isAfter(rrule.getDtend())) {
      throw new BadRequestException("Event ends after RRule dtend");
    }
    if (Boolean.FALSE.equals(rrule.getIsEnabled())) {
      throw new BadRequestException("RRule is disabled");
    }
  }
}

