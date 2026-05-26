package com.peti.backend.service.event;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.CaretakerRRuleRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import com.peti.backend.service.rrule.RRuleUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Finds RRules for a caretaker that match the requested time range and slot type.
 * An RRule matches if the requested time-of-day window overlaps
 * the RRule's [slotStartTime, slotStartTime + slotDuration) window.
 */
@Component
@RequiredArgsConstructor
public class RRuleMatcher {

  private final CaretakerRRuleRepository rruleRepository;

  /**
   * Finds all enabled RRules for the caretaker with the given slotType
   * whose time window overlaps the requested period.
   *
   * @throws BadRequestException if no matching RRules found
   */
  public List<CaretakerRRule> findMatchingRules(UUID caretakerId, String slotType,
      LocalDateTime from, LocalDateTime to) {

    List<CaretakerRRule> candidates = rruleRepository
        .findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(caretakerId, slotType);

    List<CaretakerRRule> matching = candidates.stream()
        .filter(rule -> RRuleUtils.isActiveOnDate(rule, from.toLocalDate()))
        .filter(rule -> overlapsTimeWindow(rule, from, to))
        .toList();

    if (matching.isEmpty()) {
      throw new BadRequestException(
          "No available schedule found for caretaker with slot type '"
              + slotType + "' in the requested time range");
    }

    return matching;
  }

  private boolean overlapsTimeWindow(CaretakerRRule rule,
      LocalDateTime from, LocalDateTime to) {
    LocalTime ruleStart = rule.getSlotStartTime();
    LocalTime ruleEnd = ruleStart.plus(rule.getSlotDuration());

    LocalTime requestStart = from.toLocalTime();
    LocalTime requestEnd = to.toLocalTime();

    // Overlap check: two intervals overlap if one starts before the other ends
    return requestStart.isBefore(ruleEnd) && ruleStart.isBefore(requestEnd);
  }
}

