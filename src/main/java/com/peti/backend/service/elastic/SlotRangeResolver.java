package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.LocalTime;
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
   * For each capacity level 1..maxCapacity, build a list of continuous time ranges
   * where available capacity is at least that level.
   */
  public static Map<Integer, List<TimeRange>> resolveRangesByCapacity(List<TimeSegmentWithPricing> segments) {
    int maxCapacity = segments.stream().mapToInt(TimeSegmentWithPricing::capacity).max().orElse(0);
    Map<Integer, List<TimeRange>> result = new TreeMap<>();
    for (int required = 1; required <= maxCapacity; required++) {
      result.put(required, findContinuousRanges(segments, required));
    }
    return result;
  }

  /**
   * Return the ServiceConfig of the first segment whose range covers the start of the given range.
   */
  public static ServiceConfig findServiceConfigForRange(List<TimeSegmentWithPricing> segments, TimeRange range) {
    for (TimeSegmentWithPricing segment : segments) {
      if (!segment.timeFrom().isAfter(range.timeFrom()) && segment.timeTo().isAfter(range.timeFrom())) {
        return segment.serviceConfig();
      }
    }
    return null;
  }

  private static List<TimeRange> findContinuousRanges(List<TimeSegmentWithPricing> segments, int required) {
    List<TimeRange> ranges = new ArrayList<>();
    LocalTime rangeStart = null;
    LocalTime rangeEnd = null;

    for (TimeSegmentWithPricing segment : segments) {
      if (segment.capacity() >= required) {
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

