package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.service.elastic.model.TimeRange;
import com.peti.backend.service.elastic.model.TimeSegmentWithPricing;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotRangeResolverTest {

  private static final ServiceConfig CONFIG = new ServiceConfig(
      ServiceType.WALKING, false, true, false, 3,
      Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
      Map.of(), List.of()
  );

  @Test
  @DisplayName("Uniform capacity - one range per capacity level")
  void uniformCapacity() {
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(20, 0), 3, CONFIG)
    );

    Map<Integer, List<TimeRange>> result = SlotRangeResolver.resolveRangesByCapacity(segments);

    assertThat(result).hasSize(3);
    for (int cap = 1; cap <= 3; cap++) {
      assertThat(result.get(cap)).hasSize(1);
      assertThat(result.get(cap).get(0).timeFrom()).isEqualTo(LocalTime.of(8, 0));
      assertThat(result.get(cap).get(0).timeTo()).isEqualTo(LocalTime.of(20, 0));
    }
  }

  @Test
  @DisplayName("Capacity dip creates split ranges at higher levels")
  void capacityDipSplitsHigherLevels() {
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(10, 0), 3, CONFIG),
        new TimeSegmentWithPricing(LocalTime.of(10, 0), LocalTime.of(12, 0), 1, CONFIG),
        new TimeSegmentWithPricing(LocalTime.of(12, 0), LocalTime.of(20, 0), 3, CONFIG)
    );

    Map<Integer, List<TimeRange>> result = SlotRangeResolver.resolveRangesByCapacity(segments);

    // cap=1 : one continuous 8-20
    assertThat(result.get(1)).hasSize(1);
    assertThat(result.get(1).get(0)).isEqualTo(new TimeRange(LocalTime.of(8, 0), LocalTime.of(20, 0)));

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

  @Test
  @DisplayName("findServiceConfigForRange returns correct config")
  void findServiceConfigForRange() {
    ServiceConfig other = new ServiceConfig(
        ServiceType.SITTING, false, false, false, 1,
        Duration.ofMinutes(60), Duration.ofMinutes(30), Duration.ofHours(1),
        Map.of(), List.of()
    );
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(14, 0), 3, CONFIG),
        new TimeSegmentWithPricing(LocalTime.of(14, 0), LocalTime.of(20, 0), 2, other)
    );

    assertThat(SlotRangeResolver.findServiceConfigForRange(segments,
        new TimeRange(LocalTime.of(8, 0), LocalTime.of(14, 0)))).isEqualTo(CONFIG);
    assertThat(SlotRangeResolver.findServiceConfigForRange(segments,
        new TimeRange(LocalTime.of(14, 0), LocalTime.of(20, 0)))).isEqualTo(other);
  }

  @Test
  @DisplayName("findServiceConfigForRange returns null for unmatched range")
  void findServiceConfig_noMatch() {
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(14, 0), 3, CONFIG)
    );
    assertThat(SlotRangeResolver.findServiceConfigForRange(segments,
        new TimeRange(LocalTime.of(20, 0), LocalTime.of(22, 0)))).isNull();
  }
}

