package com.peti.backend.service.elastic;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.CaretakerPreferences;
import com.peti.backend.model.elastic.ElasticSlotDocument.PricingConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for generating capacity-layered slots from MULTIPLE RRules and bookings.
 * 
 * <p>Supports multiple RRules per day (e.g., Monday 8-20 + special 18-22 extension).
 * When RRules overlap, capacity is SUMMED (assumption: different time slots = additive capacity).
 * 
 * <p>Example with 2 RRules on same day:
 * <ul>
 *   <li>RRule1: 8:00-20:00, capacity 3</li>
 *   <li>RRule2: 18:00-22:00, capacity 2</li>
 *   <li>Result: 8-18 (cap 3), 18-20 (cap 5), 20-22 (cap 2)</li>
 * </ul>
 * 
 * <p>Bookings reduce capacity from the combined total.
 */
@Slf4j
@Service
public class SlotGenerationService {

  /**
   * Input data representing the base availability from RRule.
   * Multiple RRules can be active on the same day.
   */
  public record RRuleInput(
      String ruleId,           // Unique identifier for this RRule (e.g., "base-weekday", "special-extension")
      LocalTime timeFrom,
      LocalTime timeTo,
      int maxCapacity,
      PricingConfig pricingConfig,
      int priority             // Higher priority pricing used when RRules overlap (higher number = higher priority)
  ) {
    // Convenience constructor for backward compatibility
    public RRuleInput(LocalTime timeFrom, LocalTime timeTo, int maxCapacity, PricingConfig pricingConfig) {
      this(UUID.randomUUID().toString(), timeFrom, timeTo, maxCapacity, pricingConfig, 0);
    }
  }

  /**
   * Input data representing a booking that reduces capacity.
   */
  public record BookingInput(
      LocalTime timeFrom,
      LocalTime timeTo,
      int bookedCapacity
  ) {}

  /**
   * Caretaker information for slot enrichment.
   */
  public record CaretakerInput(
      String caretakerId,
      String firstName,
      String lastName,
      Integer rating,
      String cityId,
      String cityName,
      CaretakerPreferences preferences
  ) {}

  /**
   * Internal record combining capacity with pricing info for timeline.
   */
  private record CapacityWithPricing(int capacity, PricingConfig pricingConfig) {}

  /**
   * Internal record representing a time segment with pricing info.
   */
  private record TimeSegmentWithPricing(
      LocalTime timeFrom,
      LocalTime timeTo,
      int capacity,
      PricingConfig pricingConfig
  ) {}

  /**
   * Generate all capacity-layered slots for a single caretaker on a single day.
   * This is the main atomic operation that should be called whenever:
   * - A new booking is created
   * - A booking is modified
   * - A booking is cancelled
   * - RRule changes
   *
   * @param date the date for which to generate slots
   * @param rrules list of availability rules for this day (can be multiple overlapping rules)
   * @param bookings list of existing bookings that reduce capacity
   * @param caretaker caretaker information for slot enrichment
   * @return list of generated slots with various capacity levels
   */
  public List<ElasticSlotDocument> generateSlotsForDay(
      LocalDate date,
      List<RRuleInput> rrules,
      List<BookingInput> bookings,
      CaretakerInput caretaker
  ) {
    if (rrules == null || rrules.isEmpty()) {
      return List.of();
    }

    // Filter out invalid rules
    List<RRuleInput> validRules = rrules.stream()
        .filter(r -> r.maxCapacity() > 0)
        .filter(r -> r.timeFrom().isBefore(r.timeTo()))
        .toList();
    
    if (validRules.isEmpty()) {
      return List.of();
    }

    // Step 1: Build capacity timeline merging MULTIPLE RRules and bookings
    Map<LocalTime, CapacityWithPricing> capacityTimeline = buildCapacityTimelineMultipleRules(validRules, bookings);

    // Step 2: Convert timeline to segments with pricing
    List<TimeSegmentWithPricing> segments = buildSegmentsWithPricing(capacityTimeline);

    // Step 3: Determine max capacity across all segments
    int maxCapacityOverall = segments.stream()
        .mapToInt(TimeSegmentWithPricing::capacity)
        .max()
        .orElse(0);

    if (maxCapacityOverall == 0) {
      return List.of();
    }

    // Step 4: For each capacity level (1 to maxCapacity), identify continuous time ranges
    Map<Integer, List<TimeRange>> capacityRanges = new TreeMap<>();
    for (int requiredCapacity = 1; requiredCapacity <= maxCapacityOverall; requiredCapacity++) {
      List<TimeRange> ranges = findContinuousRangesForCapacity(segments, requiredCapacity);
      capacityRanges.put(requiredCapacity, ranges);
    }

    // Step 5: Generate ElasticSlotDocument for each capacity level and time range
    List<ElasticSlotDocument> slots = new ArrayList<>();
    Instant now = Instant.now();

    for (Map.Entry<Integer, List<TimeRange>> entry : capacityRanges.entrySet()) {
      int capacity = entry.getKey();
      List<TimeRange> ranges = entry.getValue();

      for (TimeRange range : ranges) {
        // Find the pricing config for this range (use the one from the first segment that overlaps)
        PricingConfig pricing = findPricingForRange(segments, range);
        if (pricing == null) {
          continue; // Skip if no pricing found
        }

        ElasticSlotDocument slot = ElasticSlotDocument.builder()
            .id(null) // ID will be set by Elasticsearch
            .caretakerId(caretaker.caretakerId())
            .caretakerFirstName(caretaker.firstName())
            .caretakerLastName(caretaker.lastName())
            .caretakerRating(caretaker.rating())
            .caretakerCityId(caretaker.cityId())
            .caretakerCityName(caretaker.cityName())
            .caretakerPreferences(caretaker.preferences())
            .date(date)
            .timeFrom(range.timeFrom())
            .timeTo(range.timeTo())
            .pricingConfig(pricing)
            .capacity(capacity)
            .createdAt(now)
            .updatedAt(now)
            .build();
        slots.add(slot);
      }
    }

    return slots;
  }

  /**
   * Build capacity timeline by merging MULTIPLE RRules and subtracting bookings.
   * When RRules overlap, capacities are SUMMED.
   * Returns a map of time -> (capacity, pricingConfig).
   */
  private Map<LocalTime, CapacityWithPricing> buildCapacityTimelineMultipleRules(
      List<RRuleInput> rrules, 
      List<BookingInput> bookings
  ) {
    // Collect all unique time points from all RRules and bookings
    TreeMap<LocalTime, CapacityWithPricing> timeline = new TreeMap<>();

    // First pass: Add all time boundaries from RRules
    for (RRuleInput rrule : rrules) {
      timeline.putIfAbsent(rrule.timeFrom(), new CapacityWithPricing(0, null));
      timeline.putIfAbsent(rrule.timeTo(), new CapacityWithPricing(0, null));
    }

    // Add time boundaries from bookings
    for (BookingInput booking : bookings) {
      timeline.putIfAbsent(booking.timeFrom(), new CapacityWithPricing(0, null));
      timeline.putIfAbsent(booking.timeTo(), new CapacityWithPricing(0, null));
    }

    // Second pass: Calculate capacity at each time point
    for (LocalTime time : new ArrayList<>(timeline.keySet())) {
      int totalCapacity = 0;
      PricingConfig selectedPricing = null;
      int highestPriority = Integer.MIN_VALUE;

      // Sum capacity from all active RRules at this time
      for (RRuleInput rrule : rrules) {
        if (!time.isBefore(rrule.timeFrom()) && time.isBefore(rrule.timeTo())) {
          totalCapacity += rrule.maxCapacity();
          
          // Select pricing from highest priority active RRule
          if (rrule.priority() > highestPriority) {
            highestPriority = rrule.priority();
            selectedPricing = rrule.pricingConfig();
          } else if (rrule.priority() == highestPriority && selectedPricing == null) {
            selectedPricing = rrule.pricingConfig();
          }
        }
      }

      // Subtract capacity for all active bookings at this time
      for (BookingInput booking : bookings) {
        if (!time.isBefore(booking.timeFrom()) && time.isBefore(booking.timeTo())) {
          totalCapacity -= booking.bookedCapacity();
        }
      }

      // Store capacity and pricing (only if capacity > 0)
      if (totalCapacity > 0 && selectedPricing != null) {
        timeline.put(time, new CapacityWithPricing(Math.max(0, totalCapacity), selectedPricing));
      } else {
        timeline.put(time, new CapacityWithPricing(0, selectedPricing));
      }
    }

    return timeline;
  }

  /**
   * Convert timeline map to list of segments with pricing.
   */
  private List<TimeSegmentWithPricing> buildSegmentsWithPricing(Map<LocalTime, CapacityWithPricing> timeline) {
    List<TimeSegmentWithPricing> segments = new ArrayList<>();
    
    List<LocalTime> times = new ArrayList<>(timeline.keySet());
    times.sort(Comparator.naturalOrder());
    
    for (int i = 0; i < times.size() - 1; i++) {
      LocalTime segmentStart = times.get(i);
      LocalTime segmentEnd = times.get(i + 1);
      CapacityWithPricing capacityInfo = timeline.get(segmentStart);
      
      if (segmentStart.isBefore(segmentEnd) && capacityInfo.capacity() > 0 && capacityInfo.pricingConfig() != null) {
        segments.add(new TimeSegmentWithPricing(
            segmentStart, segmentEnd, capacityInfo.capacity(), capacityInfo.pricingConfig()
        ));
      }
    }
    
    return segments;
  }

  /**
   * Find pricing config for a given time range by looking at overlapping segments.
   * Returns pricing from the first segment that overlaps with the range.
   */
  private PricingConfig findPricingForRange(List<TimeSegmentWithPricing> segments, TimeRange range) {
    for (TimeSegmentWithPricing segment : segments) {
      // Check if segment overlaps with range
      if (!segment.timeFrom().isAfter(range.timeFrom()) && segment.timeTo().isAfter(range.timeFrom())) {
        return segment.pricingConfig();
      }
    }
    return null;
  }

  /**
   * Find all continuous time ranges where capacity >= required level.
   */
  private List<TimeRange> findContinuousRangesForCapacity(
      List<TimeSegmentWithPricing> segments, 
      int requiredCapacity
  ) {
    List<TimeRange> ranges = new ArrayList<>();
    
    LocalTime rangeStart = null;
    LocalTime rangeEnd = null;
    
    for (TimeSegmentWithPricing segment : segments) {
      if (segment.capacity() >= requiredCapacity) {
        // This segment has enough capacity
        if (rangeStart == null) {
          // Start a new range
          rangeStart = segment.timeFrom();
          rangeEnd = segment.timeTo();
        } else {
          // Extend the current range
          rangeEnd = segment.timeTo();
        }
      } else {
        // This segment doesn't have enough capacity
        if (rangeStart != null) {
          // Close the current range
          ranges.add(new TimeRange(rangeStart, rangeEnd));
          rangeStart = null;
          rangeEnd = null;
        }
      }
    }
    
    // Close any remaining range
    if (rangeStart != null) {
      ranges.add(new TimeRange(rangeStart, rangeEnd));
    }
    
    return ranges;
  }

  /**
   * Simple data class for time ranges.
   */
  private record TimeRange(LocalTime timeFrom, LocalTime timeTo) {}

  private LocalTime maxTime(LocalTime a, LocalTime b) {
    return a.isAfter(b) ? a : b;
  }

  private LocalTime minTime(LocalTime a, LocalTime b) {
    return a.isBefore(b) ? a : b;
  }
}
