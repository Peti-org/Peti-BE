package com.peti.backend.service.event;

import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.exception.BadRequestException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validates inputs for event creation: pet ownership and time ordering.
 */
@Component
public class EventValidator {

  /**
   * Validates that datetimeFrom is strictly before datetimeTo.
   */
  public void validateTimeOrder(LocalDateTime from, LocalDateTime to) {
    if (!from.isBefore(to)) {
      throw new BadRequestException("datetimeFrom must be strictly before datetimeTo");
    }
  }

  /**
   * Validates that all pets belong to the given user.
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
}

