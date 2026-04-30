package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.TimeRange;
import com.peti.backend.model.elastic.model.TimeSegmentWithPricing;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ElasticSlotAssemblerTest {

  private ElasticSlotAssembler assembler;
  private Caretaker caretaker;
  private ServiceConfig walkingConfig;

  @BeforeEach
  void setUp() {
    assembler = new ElasticSlotAssembler();

    walkingConfig = new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3,
        Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
        Map.of(), List.of()
    );

    City city = new City();
    city.setCityId(1L);
    city.setCity("Київ");

    User user = new User();
    user.setUserId(UUID.randomUUID());
    user.setFirstName("Іван");
    user.setLastName("Шевченко");
    user.setCityByCityId(city);

    caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());
    caretaker.setRating(5);
    caretaker.setUserReference(user);
    caretaker.setCaretakerPreference(new CaretakerPreferences(List.of(walkingConfig), Map.of()));
  }

  @Test
  @DisplayName("Assembles correct number of slots from capacity ranges")
  void assemblesCorrectSlots() {
    LocalDate date = LocalDate.of(2026, 3, 1);
    Map<Integer, List<TimeRange>> capacityRanges = Map.of(
        3, List.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(20, 0))),
        2, List.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(20, 0)))
    );
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(20, 0), 3, walkingConfig)
    );

    List<ElasticSlotDocument> result = assembler.assemble(date, capacityRanges, segments, caretaker);

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(s -> s.getDate().equals(date));
    assertThat(result).allMatch(s -> s.getCaretakerId().equals(caretaker.getCaretakerId().toString()));
    assertThat(result).allMatch(s -> s.getCaretakerFirstName().equals("Іван"));
    assertThat(result).allMatch(s -> s.getCaretakerCityId().equals("1"));
    assertThat(result).allMatch(s -> s.getCaretakerCityName().equals("Київ"));
    assertThat(result).allMatch(s -> s.getServiceConfig().equals(walkingConfig));
  }

  @Test
  @DisplayName("Splits ranges produce multiple slots per capacity level")
  void splitRanges() {
    LocalDate date = LocalDate.of(2026, 3, 1);
    Map<Integer, List<TimeRange>> capacityRanges = Map.of(
        2, List.of(
            new TimeRange(LocalTime.of(8, 0), LocalTime.of(10, 0)),
            new TimeRange(LocalTime.of(14, 0), LocalTime.of(20, 0))
        )
    );
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(10, 0), 2, walkingConfig),
        new TimeSegmentWithPricing(LocalTime.of(14, 0), LocalTime.of(20, 0), 2, walkingConfig)
    );

    List<ElasticSlotDocument> result = assembler.assemble(date, capacityRanges, segments, caretaker);

    assertThat(result).hasSize(2);
    assertThat(result).anyMatch(s ->
        s.getTimeFrom().equals(LocalTime.of(8, 0)) && s.getTimeTo().equals(LocalTime.of(10, 0)));
    assertThat(result).anyMatch(s ->
        s.getTimeFrom().equals(LocalTime.of(14, 0)) && s.getTimeTo().equals(LocalTime.of(20, 0)));
  }

  @Test
  @DisplayName("Empty capacity ranges produce no slots")
  void emptyRanges() {
    List<ElasticSlotDocument> result = assembler.assemble(
        LocalDate.of(2026, 3, 1), Map.of(), List.of(), caretaker);
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("User without city - cityId and cityName are null")
  void userWithoutCity() {
    caretaker.getUserReference().setCityByCityId(null);
    LocalDate date = LocalDate.of(2026, 3, 1);
    Map<Integer, List<TimeRange>> ranges = Map.of(
        1, List.of(new TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0)))
    );
    List<TimeSegmentWithPricing> segments = List.of(
        new TimeSegmentWithPricing(LocalTime.of(8, 0), LocalTime.of(12, 0), 1, walkingConfig)
    );

    List<ElasticSlotDocument> result = assembler.assemble(date, ranges, segments, caretaker);
    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCaretakerCityId()).isNull();
    assertThat(result.get(0).getCaretakerCityName()).isNull();
  }
}

