package com.peti.backend.service.slot.builder;

import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegment;
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
  public static Map<Integer, List<TimeRange>> resolveRangesByCapacity(List<TimeSegment> segments) {
    int maxCapacity = segments.stream().mapToInt(TimeSegment::capacity).max().orElse(0);
    Map<Integer, List<TimeRange>> result = new TreeMap<>();
    for (int capacity = 1; capacity <= maxCapacity; capacity++) {
      result.put(capacity, findContinuousRanges(segments, capacity));
    }
    return result;
  }

  /**
   * Scans segments left-to-right. Opens a range when capacity meets the threshold,
   * extends it while consecutive segments qualify, and closes it on the first gap or drop below threshold.
   */
  private static List<TimeRange> findContinuousRanges(List<TimeSegment> segments, int selectedCapacity) {
    List<TimeRange> ranges = new ArrayList<>();
    LocalDateTime rangeStart = null;
    LocalDateTime rangeEnd = null;

    for (TimeSegment segment : segments) {
      if (segment.capacity() >= selectedCapacity) {
        if (rangeStart == null) {
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

