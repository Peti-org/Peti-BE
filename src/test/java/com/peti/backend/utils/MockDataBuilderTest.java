package com.peti.backend.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MockDataBuilderTest {

  private MockDataBuilder builder;

  @BeforeEach
  void setUp() {
    builder = new MockDataBuilder();
  }

  // ---------- buildDefaultRole ----------

  @Test
  @DisplayName("buildDefaultRole - matches expected fixture")
  void buildDefaultRole_matchesFixture() {
    Role expected = ResourceLoader.loadResource("mock-builder-role.json", Role.class);

    Role actual = builder.buildDefaultRole();

    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  // ---------- populateUser ----------

  @Test
  @DisplayName("populateUser - populated fields match expected fixture for index 0")
  void populateUser_matchesFixture() {
    User expected = ResourceLoader.loadResource("mock-builder-user.json", User.class);
    User actual = new User();
    City city = new City();
    city.setCityId(1L);

    builder.populateUser(actual, 0, city);

    assertThat(actual)
        .usingRecursiveComparison()
        .comparingOnlyFields("firstName", "lastName", "email", "password", "userDataFolder")
        .isEqualTo(expected);
    assertThat(actual.getCityByCityId()).isSameAs(city);
  }

  @Test
  @DisplayName("populateUser - different indices produce different emails")
  void populateUser_differentEmails() {
    User u1 = new User();
    User u2 = new User();
    City city = new City();

    builder.populateUser(u1, 0, city);
    builder.populateUser(u2, 5, city);

    assertThat(u1.getEmail()).isNotEqualTo(u2.getEmail());
  }

  // ---------- buildCaretaker ----------

  @Test
  @DisplayName("buildCaretaker - rating and preferences match fixture for index 0")
  void buildCaretaker_matchesFixture() {
    Caretaker expectedData = ResourceLoader.loadResource(
        "mock-builder-caretaker.json", Caretaker.class);
    CaretakerPreferences expectedPrefs = ResourceLoader.loadResource(
        "mock-builder-preferences.json", CaretakerPreferences.class);
    User user = new User();
    user.setUserId(UUID.randomUUID());

    Caretaker actual = builder.buildCaretaker(user, 0);

    assertThat(actual.getRating()).isEqualTo(expectedData.getRating());
    assertThat(actual.getUserReference()).isSameAs(user);
    assertThat(actual.getCaretakerPreference())
        .usingRecursiveComparison()
        .isEqualTo(expectedPrefs);
  }

  // ---------- buildRRule ----------

  @Test
  @DisplayName("buildRRule - matches expected fixture for index 0 and given date")
  void buildRRule_matchesFixture() {
    CaretakerRRule expected = ResourceLoader.loadResource(
        "mock-builder-rrule.json", CaretakerRRule.class);
    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());

    CaretakerRRule actual = builder.buildRRule(caretaker, LocalDate.of(2026, 5, 5), 0);

    assertThat(actual)
        .usingRecursiveComparison()
        .ignoringFields("caretaker", "createdAt")
        .isEqualTo(expected);
    assertThat(actual.getCaretaker()).isSameAs(caretaker);
    assertThat(actual.getCreatedAt()).isNotNull();
  }

  // ---------- buildPreferences ----------

  @Test
  @DisplayName("buildPreferences - full recursive match against fixture for index 0")
  void buildPreferences_matchesFixture() {
    CaretakerPreferences expected = ResourceLoader.loadResource(
        "mock-builder-preferences.json", CaretakerPreferences.class);

    CaretakerPreferences actual = builder.buildPreferences(0);

    assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
  }

  // ---------- resolveCityId ----------

  @Test
  @DisplayName("resolveCityId - cycles through city IDs with modulo")
  void resolveCityId_cycles() {
    assertThat(builder.resolveCityId(0)).isEqualTo(1L);
    assertThat(builder.resolveCityId(4)).isEqualTo(5L);
    assertThat(builder.resolveCityId(5)).isEqualTo(1L);
  }

  // ---------- mockEmail ----------

  @Test
  @DisplayName("mockEmail - returns deterministic email for index")
  void mockEmail_deterministic() {
    assertThat(builder.mockEmail(0)).isEqualTo("mock-caretaker-0@peti.com");
    assertThat(builder.mockEmail(42)).isEqualTo("mock-caretaker-42@peti.com");
  }

  // ---------- determinism ----------

  @Test
  @DisplayName("Same index always produces identical output")
  void deterministic_sameIndex() {
    MockDataBuilder b1 = new MockDataBuilder();
    MockDataBuilder b2 = new MockDataBuilder();

    assertThat(b1.buildPreferences(7))
        .usingRecursiveComparison()
        .isEqualTo(b2.buildPreferences(7));
  }
}
