package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import com.peti.backend.model.internal.TimeSlotPair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SlotDividerTest {

  private SlotDivider slotDivider;

  @BeforeEach
  void setUp() {
    slotDivider = new SlotDivider();
  }

  @Test
  void testDivideTimeRange_30MinIntervals() {
    LocalTime start = LocalTime.of(9, 10);
    LocalTime end = LocalTime.of(12, 45);
    List<TimeSlotPair> slots = slotDivider.divideTimeRange(start, end, 30);

    assertEquals(6, slots.size());
    assertEquals(LocalTime.of(9, 30), slots.get(0).startTime());
    assertEquals(LocalTime.of(10, 0), slots.get(0).endTime());
    assertEquals(LocalTime.of(12, 0), slots.get(5).startTime());
    assertEquals(LocalTime.of(12, 30), slots.get(5).endTime());
  }

  @Test
  void testDivideTimeRange_15MinIntervals() {
    LocalTime start = LocalTime.of(8, 7);
    LocalTime end = LocalTime.of(9, 46);
    List<TimeSlotPair> slots = slotDivider.divideTimeRange(start, end, 15);

    assertEquals(6, slots.size());
    assertEquals(LocalTime.of(8, 15), slots.get(0).startTime());
    assertEquals(LocalTime.of(8, 30), slots.get(0).endTime());
    assertEquals(LocalTime.of(9, 30), slots.get(5).startTime());
    assertEquals(LocalTime.of(9, 45), slots.get(5).endTime());
  }

  @Test
  void testDivideTimeRange_ExactBoundary() {
    LocalTime start = LocalTime.of(10, 0);
    LocalTime end = LocalTime.of(11, 0);
    List<TimeSlotPair> slots = slotDivider.divideTimeRange(start, end, 30);

    assertEquals(2, slots.size());
    assertEquals(LocalTime.of(10, 0), slots.get(0).startTime());
    assertEquals(LocalTime.of(10, 30), slots.get(0).endTime());
    assertEquals(LocalTime.of(10, 30), slots.get(1).startTime());
    assertEquals(LocalTime.of(11, 0), slots.get(1).endTime());
  }

  @Test
  void testDivideTimeRange_NoSlotsIfNormalizedStartAfterEnd() {
    LocalTime start = LocalTime.of(10, 5);
    LocalTime end = LocalTime.of(10, 10);
    List<TimeSlotPair> slots = slotDivider.divideTimeRange(start, end, 30);

    assertTrue(slots.isEmpty());
  }

  @Test
  void testDivideTimeRange_InvalidInterval_Throws() {
    assertThrows(IllegalArgumentException.class, () ->
        slotDivider.divideTimeRange(LocalTime.of(8, 0), LocalTime.of(9, 0), 0));
    assertThrows(IllegalArgumentException.class, () ->
        slotDivider.divideTimeRange(LocalTime.of(8, 0), LocalTime.of(9, 0), 1441));
  }
}
