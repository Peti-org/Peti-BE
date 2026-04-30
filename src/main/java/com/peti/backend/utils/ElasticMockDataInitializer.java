package com.peti.backend.utils;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.BreedConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.DaySchedule;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PetConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PickupDelivery;
import com.peti.backend.dto.caretacker.CaretakerPreferences.PriceInfo;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.caretacker.CaretakerPreferences.WeightTier;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.service.elastic.SlotGenerationService;
import com.peti.backend.model.elastic.model.BookingInput;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Initialises mock Elasticsearch slot data using proper domain objects.
 * Active only when {@code elasticsearch.mock-data.enabled=true}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticMockDataInitializer {

  private final ElasticSlotRepository slotRepository;
  private final SlotGenerationService slotGenerationService;
  private final org.springframework.data.elasticsearch.core.ElasticsearchOperations elasticsearchOperations;

  @Value("${elasticsearch.mock-data.enabled:true}")
  private boolean mockDataEnabled;

  @Value("${elasticsearch.mock-data.caretakers:50}")
  private int numberOfCaretakers;

  @Value("${elasticsearch.mock-data.days-ahead:60}")
  private int daysAhead;

  private static final String[] FIRST_NAMES = {
      "Олександр", "Марія", "Дмитро", "Анна", "Іван",
      "Олена", "Микола", "Тетяна", "Андрій", "Юлія",
      "Тарас", "Максим", "Федір", "Ростислав"
  };

  private static final String[] LAST_NAMES = {
      "Шевченко", "Коваленко", "Бондаренко", "Ткаченко", "Мельник",
      "Кравченко", "Олійник", "Лисенко", "Морозенко", "Петренко",
      "Лщшн", "Зварич", "Орищак"
  };

  private static final long[] CITY_IDS = {1L, 2L, 3L, 4L, 5L};
  private static final String[] CITY_NAMES = {"Київ", "Львів", "Дніпро", "Харків", "Одеса"};

  private final Random random = new Random(42);

  @EventListener(ApplicationReadyEvent.class)
  public void initializeMockData() {
    if (!mockDataEnabled) {
      return;
    }

    // Always recreate index to ensure mapping matches the current code
    var indexOps = elasticsearchOperations.indexOps(ElasticSlotDocument.class);
    if (indexOps.exists()) {
      log.info("Deleting existing walking-slots index to refresh mappings");
      indexOps.delete();
    }
    indexOps.create();
    indexOps.putMapping(indexOps.createMapping());

    log.info("Initializing Elasticsearch mock data with {} caretakers for {} days ahead",
        numberOfCaretakers, daysAhead);

    int totalSlots = 0;
    for (int i = 0; i < numberOfCaretakers; i++) {
      Caretaker caretaker = buildMockCaretaker(i);
      List<ElasticSlotDocument> slots = generateSlotsForCaretaker(caretaker);
      slotRepository.saveAll(slots);
      totalSlots += slots.size();
    }
    log.info("Mock data initialization completed. Created {} slots for {} caretakers.",
        totalSlots, numberOfCaretakers);
  }

  // ── Mock domain builders ───────────────────────────────────────────────────

  private Caretaker buildMockCaretaker(int index) {
    int cityIndex = index % CITY_NAMES.length;

    City city = new City();
    city.setCityId(CITY_IDS[cityIndex]);
    city.setCity(CITY_NAMES[cityIndex]);

    User user = new User();
    user.setUserId(UUID.randomUUID());
    user.setFirstName(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)]);
    user.setLastName(LAST_NAMES[random.nextInt(LAST_NAMES.length)]);
    user.setCityByCityId(city);

    Caretaker caretaker = new Caretaker();
    caretaker.setCaretakerId(UUID.randomUUID());
    caretaker.setRating(3 + random.nextInt(3));
    caretaker.setUserReference(user);
    caretaker.setCaretakerPreference(buildMockPreferences());
    return caretaker;
  }

  private CaretakerPreferences buildMockPreferences() {
    PriceInfo price = new PriceInfo(
        BigDecimal.valueOf(80 + random.nextInt(121)),
        BigDecimal.valueOf(20 + random.nextInt(41)),
        null,
        "UAH",
        null
    );
    WeightTier tier = new WeightTier(price, true);
    PetConfig petConfig = new PetConfig(
        true,
        new BreedConfig(true, List.of(), List.of()),
        true,
        null,
        new PickupDelivery(random.nextBoolean(), random.nextBoolean()),
        Map.of("Medium", tier)
    );
    ServiceConfig walkingConfig = new ServiceConfig(
        ServiceType.WALKING,
        false,
        true,
        false,
        2 + random.nextInt(4),
        Duration.ofHours(1),
        Duration.ofMinutes(30),
        Duration.ofMinutes(10 + random.nextInt(50)),
        Map.of("Dog", petConfig),
        List.of()
    );
    Map<String, DaySchedule> schedule = Map.of(
        "MONDAY",    new DaySchedule(true,  LocalTime.of(8, 0),  LocalTime.of(20, 0)),
        "TUESDAY",   new DaySchedule(true,  LocalTime.of(8, 0),  LocalTime.of(20, 0)),
        "WEDNESDAY", new DaySchedule(true,  LocalTime.of(9, 0),  LocalTime.of(18, 0)),
        "THURSDAY",  new DaySchedule(true,  LocalTime.of(8, 0),  LocalTime.of(20, 0)),
        "FRIDAY",    new DaySchedule(true,  LocalTime.of(9, 0),  LocalTime.of(22, 0)),
        "SATURDAY",  new DaySchedule(true,  LocalTime.of(9, 0),  LocalTime.of(18, 0)),
        "SUNDAY",    new DaySchedule(false, LocalTime.of(9, 0),  LocalTime.of(18, 0))
    );
    return new CaretakerPreferences(List.of(walkingConfig), schedule);
  }

  private CaretakerRRule buildMockRRule(Caretaker caretaker, LocalDate date) {
    int startHour = 8 + random.nextInt(2);
    int endHour   = 18 + random.nextInt(4);

    CaretakerRRule rule = new CaretakerRRule();
    rule.setRruleId(UUID.randomUUID());
    rule.setCaretaker(caretaker);
    rule.setRrule("FREQ=DAILY");
    rule.setDtstart(LocalDateTime.of(date, LocalTime.of(startHour, 0)));
    rule.setDtend(LocalDateTime.of(date, LocalTime.of(endHour, 0)));
    rule.setSlotType(ServiceType.WALKING.name());
    rule.setCapacity(2 + random.nextInt(3));
    rule.setIntervalMinutes(30);
    rule.setIsEnabled(true);
    rule.setIsSchedule(true);
    rule.setIsBusy(false);
    rule.setPriority(0);
    rule.setCreatedAt(LocalDateTime.now());
    return rule;
  }

  // ── Slot generation ────────────────────────────────────────────────────────

  private List<ElasticSlotDocument> generateSlotsForCaretaker(Caretaker caretaker) {
    List<ElasticSlotDocument> allSlots = new ArrayList<>();
    LocalDate current = LocalDate.now();
    LocalDate end = current.plusDays(daysAhead);

    while (!current.isAfter(end)) {
      if (current.getDayOfWeek() == DayOfWeek.SUNDAY && random.nextBoolean()) {
        current = current.plusDays(1);
        continue;
      }
      CaretakerRRule rrule = buildMockRRule(caretaker, current);
      List<BookingInput> bookings = buildRandomBookings(rrule);
      allSlots.addAll(slotGenerationService.generateSlotsForDay(current, List.of(rrule), bookings, caretaker));
      current = current.plusDays(1);
    }
    return allSlots;
  }

  private List<BookingInput> buildRandomBookings(CaretakerRRule rrule) {
    int count = random.nextInt(3);
    List<BookingInput> bookings = new ArrayList<>();
    LocalTime from = rrule.getDtstart().toLocalTime();
    LocalTime to   = rrule.getDtend().toLocalTime();
    int availableHours = to.getHour() - from.getHour();

    if (availableHours < 2 || count == 0) {
      return bookings;
    }
    for (int i = 0; i < count; i++) {
      int offset   = random.nextInt(availableHours - 1);
      int duration = 1 + random.nextInt(Math.min(3, availableHours - offset));
      int booked   = 1 + random.nextInt(Math.max(1, rrule.getCapacity() - 1));
      LocalTime start  = from.plusHours(offset);
      LocalTime finish = start.plusHours(duration);
      if (finish.isAfter(to)) finish = to;
      bookings.add(new BookingInput(start, finish, booked));
    }
    return bookings;
  }
}

