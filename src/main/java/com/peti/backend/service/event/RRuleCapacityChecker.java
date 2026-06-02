package com.peti.backend.service.event;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.EventRepository;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Validates capacity using a sweep-line approach across the requested time range.
 * Splits the range into sub-intervals based on RRule time boundaries, then for each
 * sub-interval calculates: (sum of covering RRule capacities) - (sum of pets from
 * overlapping events). The minimum across all sub-intervals is the available capacity.
 */

//todo need to refactor to optimise logic and buisness needs at all
  //possibly upgrade efficiency to nlog n if could be
@Component
@RequiredArgsConstructor
public class RRuleCapacityChecker {

  private final EventRepository eventRepository;

  /**
   * Validates that available capacity (min across all sub-intervals) is sufficient.
   *
   * @param matchingRules RRules overlapping the requested time range
   * @param caretakerId   the caretaker
   * @param from          event start
   * @param to            event end
   * @param petCount      number of pets being booked
   * @throws BadRequestException if capacity is insufficient at any point in the range
   */
  public void validateCapacity(List<CaretakerRRule> matchingRules,
      UUID caretakerId, LocalDateTime from, LocalDateTime to, int petCount) {

    List<Event> overlappingEvents = eventRepository.findApprovedOverlapping(
        caretakerId, from, to);

    List<LocalTime> boundaries = buildBoundaries(matchingRules, from, to);
    int minAvailable = calculateMinAvailableCapacity(
        boundaries, matchingRules, overlappingEvents);

    if (petCount > minAvailable) {
      throw new BadRequestException(
          "Insufficient capacity. Available: " + minAvailable
              + ", requested: " + petCount);
    }
  }

  /**
   * Builds sorted unique time boundaries from RRule edges and requested range edges.
   */
  private List<LocalTime> buildBoundaries(List<CaretakerRRule> rules,
      LocalDateTime from, LocalDateTime to) {
    List<LocalTime> points = new ArrayList<>();
    points.add(from.toLocalTime());
    points.add(to.toLocalTime());

    for (CaretakerRRule rule : rules) {
      LocalTime ruleStart = rule.getSlotStartTime();
      LocalTime ruleEnd = ruleStart.plus(rule.getSlotDuration());
      points.add(ruleStart);
      points.add(ruleEnd);
    }

    return points.stream()
        .distinct()
        .sorted()
        .filter(t -> !t.isBefore(from.toLocalTime()) && !t.isAfter(to.toLocalTime()))
        .toList();
  }

  /**
   * For each sub-interval between consecutive boundaries, calculates the available
   * capacity and returns the minimum.
   */
  private int calculateMinAvailableCapacity(List<LocalTime> boundaries,
      List<CaretakerRRule> rules, List<Event> events) {

    int minAvailable = Integer.MAX_VALUE;

    for (int i = 0; i < boundaries.size() - 1; i++) {
      LocalTime intervalStart = boundaries.get(i);
      LocalTime intervalEnd = boundaries.get(i + 1);

      if (!intervalStart.isBefore(intervalEnd)) {
        continue;
      }

      int capacity = sumCapacityForInterval(rules, intervalStart, intervalEnd);
      int used = sumUsedForInterval(events, intervalStart, intervalEnd);
      int available = capacity - used;

      minAvailable = Math.min(minAvailable, available);
    }

    return minAvailable == Integer.MAX_VALUE ? 0 : minAvailable;
  }

  private int sumCapacityForInterval(List<CaretakerRRule> rules,
      LocalTime intervalStart, LocalTime intervalEnd) {
    return rules.stream()
        .filter(rule -> coversInterval(rule, intervalStart, intervalEnd))
        .mapToInt(CaretakerRRule::getPetCapacity)
        .sum();
  }

  private boolean coversInterval(CaretakerRRule rule,
      LocalTime intervalStart, LocalTime intervalEnd) {
    LocalTime ruleStart = rule.getSlotStartTime();
    LocalTime ruleEnd = ruleStart.plus(rule.getSlotDuration());
    return !ruleStart.isAfter(intervalStart) && !ruleEnd.isBefore(intervalEnd);
  }

  private int sumUsedForInterval(List<Event> events,
      LocalTime intervalStart, LocalTime intervalEnd) {
    return events.stream()
        .filter(e -> eventOverlapsInterval(e, intervalStart, intervalEnd))
        .mapToInt(e -> e.getPets() != null ? e.getPets().size() : 0)
        .sum();
  }

  private boolean eventOverlapsInterval(Event event,
      LocalTime intervalStart, LocalTime intervalEnd) {
    // Event time range already known to overlap [from, to] (from repository query).
    // Check if event's time-of-day range overlaps this specific sub-interval.
    LocalTime eventStart = event.getDatetimeFrom().toLocalTime();
    LocalTime eventEnd = event.getDatetimeTo().toLocalTime();
    return eventStart.isBefore(intervalEnd) && eventEnd.isAfter(intervalStart);
  }
}

