package com.peti.backend.service.event;

import com.peti.backend.dto.caretaker.CaretakerPreferences;
import com.peti.backend.dto.caretaker.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.exception.BadRequestException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Validates inputs for event creation: time ordering, pet ownership and event duration.
 */
@Component
public class EventValidator {

  public void validateTimeOrder(LocalDateTime from, LocalDateTime to) {
    if (!from.isBefore(to)) {
      throw new BadRequestException("datetimeFrom must be strictly before datetimeTo");
    }
  }

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
   * Ensures requested event duration equals {@code baseDuration + n * stepDuration}
   * (with {@code n >= 0}) defined in the caretaker's preferences for {@code slotType}.
   */
  public void validateDuration(Caretaker caretaker, String slotType,
      LocalDateTime from, LocalDateTime to) {
    ServiceConfig service = resolveServiceConfig(caretaker, slotType);
    Duration base = requirePositive(service.baseDuration(), "base", slotType);
    Duration step = requireNonNegative(service.stepDuration(), "step", slotType);
    Duration requested = Duration.between(from, to);

    requireAtLeastBase(requested, base);
    requireAlignedToStep(requested, base, step);
  }

  private Duration requirePositive(Duration value, String label, String slotType) {
    if (value == null || value.isZero() || value.isNegative()) {
      throw new BadRequestException(
          "Caretaker preferences contain an invalid " + label
              + " duration for slot type: " + slotType);
    }
    return value;
  }

  private Duration requireNonNegative(Duration value, String label, String slotType) {
    if (value == null || value.isNegative()) {
      throw new BadRequestException(
          "Caretaker preferences contain an invalid " + label
              + " duration for slot type: " + slotType);
    }
    return value;
  }

  private void requireAtLeastBase(Duration requested, Duration base) {
    if (requested.compareTo(base) < 0) {
      throw new BadRequestException(
          "Event duration " + requested + " is shorter than the minimum allowed " + base);
    }
  }

  private void requireAlignedToStep(Duration requested, Duration base, Duration step) {
    Duration excess = requested.minus(base);
    boolean aligned = step.isZero()
        ? excess.isZero()
        : excess.toMillis() % step.toMillis() == 0;
    if (!aligned) {
      throw new BadRequestException(
          "Event duration " + requested + " must equal base " + base
              + " plus a non-negative multiple of step " + step);
    }
  }

  private ServiceConfig resolveServiceConfig(Caretaker caretaker, String slotType) {
    CaretakerPreferences preferences = caretaker.getCaretakerPreference();
    if (preferences == null || preferences.services() == null) {
      throw new BadRequestException("Caretaker has no service preferences configured");
    }
    ServiceConfig config = preferences.getService(slotType);
    if (config == null) {
      throw new BadRequestException(
          "Caretaker does not offer requested slot type: " + slotType);
    }
    return config;
  }
}

