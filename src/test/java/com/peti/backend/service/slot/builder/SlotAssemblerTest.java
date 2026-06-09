package com.peti.backend.service.slot.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.dto.caretaker.CaretakerPreferences;
import com.peti.backend.dto.caretaker.ServiceConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.TimeRange;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SlotAssemblerTest {

  private static final LocalDate DATE = LocalDate.of(2026, 3, 1);

  private Caretaker caretaker;
  private ServiceConfig walkingConfig;

  @BeforeEach
  void setUp() {
    walkingConfig = new ServiceConfig(
        ServiceType.WALKING, false, true, false, 3, 20,
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
    caretaker.setCaretakerPreference(new CaretakerPreferences(
        new java.util.EnumMap<>(Map.of(ServiceType.WALKING, walkingConfig))));
  }

  @Test
  @DisplayName("Assembles correct number of slots from capacity ranges")
  void assemblesCorrectSlots() {
    Map<Integer, List<TimeRange>> capacityRanges = Map.of(
        3, List.of(new TimeRange(DATE.atTime(8, 0), DATE.atTime(20, 0))),
        2, List.of(new TimeRange(DATE.atTime(8, 0), DATE.atTime(20, 0)))
    );

    List<ElasticSlotDocument> result = SlotAssembler.assemble(capacityRanges, caretaker, walkingConfig);

    assertThat(result).hasSize(2);
    assertThat(result).allMatch(s -> s.getFromDateTime().toLocalDate().equals(DATE));
    assertThat(result).allMatch(s -> s.getCaretakerId().equals(caretaker.getCaretakerId().toString()));
    assertThat(result).allMatch(s -> s.getCaretakerFirstName().equals("Іван"));
    assertThat(result).allMatch(s -> s.getCaretakerCityId().equals("1"));
    assertThat(result).allMatch(s -> s.getCaretakerCityName().equals("Київ"));
    assertThat(result).allMatch(s -> s.getServiceConfig().equals(walkingConfig));
  }

  @Test
  @DisplayName("Splits ranges produce multiple slots per capacity level")
  void splitRanges() {
    Map<Integer, List<TimeRange>> capacityRanges = Map.of(
        2, List.of(
            new TimeRange(DATE.atTime(8, 0), DATE.atTime(10, 0)),
            new TimeRange(DATE.atTime(14, 0), DATE.atTime(20, 0))
        )
    );

    List<ElasticSlotDocument> result = SlotAssembler.assemble(capacityRanges, caretaker, walkingConfig);

    assertThat(result).hasSize(2);
    assertThat(result).anyMatch(s ->
        s.getFromDateTime().equals(DATE.atTime(8, 0)) && s.getToDateTime().equals(DATE.atTime(10, 0)));
    assertThat(result).anyMatch(s ->
        s.getFromDateTime().equals(DATE.atTime(14, 0)) && s.getToDateTime().equals(DATE.atTime(20, 0)));
  }

  @Test
  @DisplayName("Empty capacity ranges produce no slots")
  void emptyRanges() {
    List<ElasticSlotDocument> result = SlotAssembler.assemble(Map.of(), caretaker, walkingConfig);
    assertThat(result).isEmpty();
  }
}
