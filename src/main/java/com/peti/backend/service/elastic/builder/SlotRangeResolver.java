package com.peti.backend.service.elastic.builder;

import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Stateless utility that resolves continuous time ranges grouped by capacity level
 * and looks up the applicable ServiceConfig for each range.
 */
public final class SlotRangeResolver {

  private SlotRangeResolver() {}

  /**
   * segments sorted by timeFrom ascending, non-overlapping, positive capacity
   * For each capacity level 1..maxCapacity, build a list of continuous time ranges
   * where available capacity is at least that level.
   */
  public static Map<Integer, List<TimeRange>> resolveRangesByCapacity(List<TimeSegmentWithPricing> segments) {
    int maxCapacity = segments.stream().mapToInt(TimeSegmentWithPricing::capacity).max().orElse(0);
    Map<Integer, List<TimeRange>> result = new TreeMap<>();
    for (int capacity = 1; capacity <= maxCapacity; capacity++) {
      result.put(capacity, findContinuousRanges(segments, capacity));
    }
    return result;
  }

  private static List<TimeRange> findContinuousRanges(List<TimeSegmentWithPricing> segments, int selectedCapacity) {
    List<TimeRange> ranges = new ArrayList<>();
    LocalDateTime rangeStart = null;
    LocalDateTime rangeEnd = null;

    for (TimeSegmentWithPricing segment : segments) {
      if (segment.capacity() >= selectedCapacity) {
        if (rangeStart == null) {//todo if one segmet is missing like 8-9,10-11,12-13 and we are looking for capacity 2, then we will have two ranges 8-9 and 12-13, but actually it should be one range 8-13
          rangeStart = segment.timeFrom();
        }
        rangeEnd = segment.timeTo();
      } else if (rangeStart != null) {
        ranges.add(new TimeRange(rangeStart, rangeEnd));
        rangeStart = null;
        rangeEnd = null;
      }
    }

    if (rangeStart != null) {
      ranges.add(new TimeRange(rangeStart, rangeEnd));
    }

    return ranges;
  }
}

