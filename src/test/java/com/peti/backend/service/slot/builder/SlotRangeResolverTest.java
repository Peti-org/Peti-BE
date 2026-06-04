package com.peti.backend.service.slot.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegment;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotRangeResolverTest {

  private static final LocalDate D = LocalDate.of(2026, 3, 1);

  @Test
  @DisplayName("Uniform capacity - one range per capacity level")
  void uniformCapacity() {
    List<TimeSegment> segments = List.of(
        new TimeSegment(D.atTime(8, 0), D.atTime(20, 0), 3)
    );

    Map<Integer, List<TimeRange>> result = SlotRangeResolver.resolveRangesByCapacity(segments);

    assertThat(result).hasSize(3);
    for (int cap = 1; cap <= 3; cap++) {
      assertThat(result.get(cap)).hasSize(1);
      assertThat(result.get(cap).get(0).timeFrom()).isEqualTo(D.atTime(8, 0));
      assertThat(result.get(cap).get(0).timeTo()).isEqualTo(D.atTime(20, 0));
    }
  }

  @Test
  @DisplayName("Capacity dip creates split ranges at higher levels")
  void capacityDipSplitsHigherLevels() {
    List<TimeSegment> segments = List.of(
        new TimeSegment(D.atTime(8, 0), D.atTime(10, 0), 3),
        new TimeSegment(D.atTime(10, 0), D.atTime(12, 0), 1),
        new TimeSegment(D.atTime(12, 0), D.atTime(20, 0), 3)
    );

    Map<Integer, List<TimeRange>> result = SlotRangeResolver.resolveRangesByCapacity(segments);

    // cap=1 : one continuous 8-20
    assertThat(result.get(1)).hasSize(1);
    assertThat(result.get(1).get(0)).isEqualTo(new TimeRange(D.atTime(8, 0), D.atTime(20, 0)));

    // cap=2 : split into 8-10 and 12-20
    assertThat(result.get(2)).hasSize(2);

    // cap=3 : split into 8-10 and 12-20
    assertThat(result.get(3)).hasSize(2);
  }

  @Test
  @DisplayName("Empty segments - empty result")
  void emptySegments() {
    Map<Integer, List<TimeRange>> result = SlotRangeResolver.resolveRangesByCapacity(List.of());
    assertThat(result).isEmpty();
  }
}
