package com.peti.backend.utils;

import com.peti.backend.dto.caretaker.BreedConfig;
import com.peti.backend.dto.caretaker.CaretakerPreferences;
import com.peti.backend.dto.caretaker.DeliveryOption;
import com.peti.backend.dto.caretaker.PetConfig;
import com.peti.backend.dto.caretaker.PriceInfo;
import com.peti.backend.dto.caretaker.ServiceConfig;
import com.peti.backend.dto.caretaker.WeightTier;
import com.peti.backend.dto.caretaker.WeightTierConfig;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.internal.ServiceType;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Builds mock domain objects for Elasticsearch slot initialization. Pure logic — no Spring/DB dependencies; fully
 * unit-testable. All values are deterministic, derived from the caretaker {@code index}.
 */
public class MockDataBuilder {

  static final String MOCK_PASSWORD_HASH = "$2a$10$mockpasswordhash000000000000000";
  static final int MOCK_ROLE_ID = 3;
  private static final String MOCK_EMAIL_PREFIX = "mock-caretaker-";
  private static final String MOCK_EMAIL_SUFFIX = "@peti.com";
  private static final String[] FIRST_NAMES = {
      "Oleksandr", "Maria", "Dmytro", "Anna", "Ivan",
      "Olena", "Mykola", "Tetiana", "Andrii", "Yulia",
      "Taras", "Maksym", "Fedir", "Rostyslav"
  };

  private static final String[] LAST_NAMES = {
      "Shevchenko", "Kovalenko", "Bondarenko", "Tkachenko", "Melnyk",
      "Kravchenko", "Oliinyk", "Lysenko", "Morozenko", "Petrenko",
      "Leshchyshyn", "Zvarych", "Oryshchak"
  };

  private static final long[] CITY_IDS = {1L, 2L, 3L, 4L, 5L};

  /**
   * Builds a {@link Role} with the given id and name.
   */
  public Role buildRole(int roleId, String roleName) {
    Role role = new Role();
    role.setRoleId(roleId);
    role.setRoleName(roleName);
    return role;
  }

  /**
   * Builds a default USER {@link Role} using the mock role id.
   */
  public Role buildDefaultRole() {
    return buildRole(MOCK_ROLE_ID, "USER");
  }

  /**
   * Populates a pre-resolved {@link User} with mock personal data derived from {@code index}.
   */
  public void populateUser(User user, int index, City city) {
    user.setFirstName(FIRST_NAMES[index % FIRST_NAMES.length]);
    user.setLastName(LAST_NAMES[index % LAST_NAMES.length]);
    user.setEmail(mockEmail(index));
    user.setBirthday(java.sql.Date.valueOf("1990-01-01"));
    user.setPassword(MOCK_PASSWORD_HASH);
    user.setCityByCityId(city);
    user.setUserDataFolder("mock-data-" + index);
  }

  /**
   * Builds a {@link Caretaker} linked to the given user with mock preferences. Rating cycles 3-5 based on index.
   */
  public Caretaker buildCaretaker(User user, int index) {
    Caretaker caretaker = new Caretaker();
    caretaker.setRating(3 + (index % 3));
    caretaker.setUserReference(user);
    caretaker.setCaretakerPreference(buildPreferences(index));
    return caretaker;
  }

  /**
   * Builds a {@link CaretakerRRule} for a specific date linked to the caretaker. Start hour cycles 8-9, end hour
   * cycles 18-21, capacity cycles 1-3.
   */
  public CaretakerRRule buildRRule(Caretaker caretaker, LocalDate date, int index) {
    int startHour = 8 + (index % 2);
    int endHour = 18 + (index % 4);

    CaretakerRRule rule = new CaretakerRRule();
    rule.setCaretaker(caretaker);
    rule.setRrule("FREQ=DAILY");
    rule.setSlotStartTime(LocalTime.of(startHour, 0));
    rule.setSlotDuration(java.time.Duration.ofHours(endHour - startHour));
    rule.setSlotType(ServiceType.WALKING.name());
    rule.setPetCapacity(1 + (index % 3));
    rule.setPeopleCapacity(20);
    rule.setIsEnabled(true);
    rule.setIsSchedule(true);
    rule.setIsBusy(false);
    rule.setPriority(0);
    rule.setCreatedAt(LocalDateTime.now());
    rule.setUpdatedAt(LocalDateTime.now());
    return rule;
  }

  /**
   * Builds a {@link CaretakerPreferences} with WALKING service config for Dog. Price and pickup settings are derived
   * from {@code index}.
   */
  CaretakerPreferences buildPreferences(int index) {
    PriceInfo price = new PriceInfo(
        BigDecimal.valueOf(80 + (index % 121)),
        BigDecimal.valueOf(20 + (index % 41)),
        null, "UAH", null
    );
    Map<WeightTier, WeightTierConfig> tiers = new EnumMap<>(WeightTier.class);
    tiers.put(WeightTier.MEDIUM, new WeightTierConfig(true, price));

    Set<DeliveryOption> delivery = EnumSet.noneOf(DeliveryOption.class);
    if (index % 2 == 0) delivery.add(DeliveryOption.PICK_UP);
    if (index % 3 == 0) delivery.add(DeliveryOption.BRING);

    PetConfig petConfig = new PetConfig(
        true,
        new BreedConfig(true, List.of(), List.of()),
        false, null,
        delivery,
        tiers
    );
    ServiceConfig walkingConfig = new ServiceConfig(
        ServiceType.WALKING, false, true, false,
        2 + (index % 4), 20,
        Duration.ofHours(1), Duration.ofMinutes(30),
        Duration.ofMinutes(10 + (index % 50)),
        Map.of("Dog", petConfig), List.of()
    );
    Map<ServiceType, ServiceConfig> services = new EnumMap<>(ServiceType.class);
    services.put(ServiceType.WALKING, walkingConfig);
    return new CaretakerPreferences(services);
  }

  /**
   * Resolves the city id from a caretaker index using modulo over available cities.
   */
  public long resolveCityId(int caretakerIndex) {
    return CITY_IDS[caretakerIndex % CITY_IDS.length];
  }

  /**
   * Returns the deterministic email for a mock caretaker at the given index.
   */
  public String mockEmail(int index) {
    return MOCK_EMAIL_PREFIX + index + MOCK_EMAIL_SUFFIX;
  }
}
