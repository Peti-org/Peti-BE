package com.peti.backend.utils;

import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.City;
import com.peti.backend.model.domain.Role;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.BookingInput;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.repository.CaretakerRepository;
import com.peti.backend.repository.CityRepository;
import com.peti.backend.repository.RoleRepository;
import com.peti.backend.repository.UserRepository;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.service.elastic.SlotGenerationService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Initialises mock Elasticsearch slot data and persists all entities to the database. Uses "find or create" pattern: if
 * a user/caretaker/city already exists, it is reused. Active only when {@code elasticsearch.mock-data.enabled=true}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(value = "elasticsearch.mock-data.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticMockDataInitializer {

  private final ElasticSlotRepository slotRepository;
  private final SlotGenerationService slotGenerationService;
  private final ElasticsearchOperations elasticsearchOperations;
  private final UserRepository userRepository;
  private final CaretakerRepository caretakerRepository;
  private final CaretakerRRuleRepository rruleRepository;
  private final CityRepository cityRepository;
  private final RoleRepository roleRepository;

  @Value("${elasticsearch.mock-data.caretakers:50}")
  private int numberOfCaretakers;
  @Value("${elasticsearch.data-generation.days-ahead:60}")
  private int daysAhead;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void initializeMockData() {
    recreateElasticIndex();
    log.info("Initializing mock data with {} caretakers for {} days ahead",
        numberOfCaretakers, daysAhead);

    MockDataBuilder builder = new MockDataBuilder();
    int totalSlots = 0;
    for (int i = 0; i < numberOfCaretakers; i++) {
      Caretaker caretaker = findOrCreateCaretaker(builder, i);
      List<ElasticSlotDocument> slots = generateSlotsForCaretaker(builder, caretaker, i);
      slotRepository.saveAll(slots);
      totalSlots += slots.size();
    }
    log.info("Mock data init complete. {} slots for {} caretakers.",
        totalSlots, numberOfCaretakers);
  }

  // ── Caretaker pipeline (top-down) ─────────────────────────────────

  private Caretaker findOrCreateCaretaker(MockDataBuilder builder, int index) {
    long cityId = builder.resolveCityId(index);
    City city = findOrCreateCity(cityId);
    User user = findOrCreateUser(builder, index, city);
    return caretakerRepository.findByUserReference_UserId(user.getUserId())
        .orElseGet(() -> caretakerRepository.save(builder.buildCaretaker(user, index)));
  }

  private User findOrCreateUser(MockDataBuilder builder, int index, City city) {
    String email = builder.mockEmail(index);
    return userRepository.findByEmail(email).orElseGet(() -> {
      Role role = findOrCreateRole(builder);
      User user = new User();
      user.setRole(role);
      builder.populateUser(user, index, city);
      return userRepository.save(user);
    });
  }

  private Role findOrCreateRole(MockDataBuilder builder) {
    return roleRepository.findById(MockDataBuilder.MOCK_ROLE_ID)
        .orElseGet(() -> roleRepository.save(builder.buildDefaultRole()));
  }

  private City findOrCreateCity(long cityId) {
    return cityRepository.findById(cityId).orElseGet(() -> {
      log.debug("City {} not found, creating stub", cityId);
      City c = new City();
      c.setCityId(cityId);
      return cityRepository.save(c);
    });
  }

  // ── Slot generation ───────────────────────────────────────────────

  private List<ElasticSlotDocument> generateSlotsForCaretaker(
      MockDataBuilder builder, Caretaker caretaker, int index) {
    List<ElasticSlotDocument> allSlots = new ArrayList<>();
    LocalDate current = LocalDate.now();
    LocalDate end = current.plusDays(daysAhead);
    while (!current.isAfter(end)) {
      CaretakerRRule rrule = builder.buildRRule(caretaker, current, index);
      CaretakerRRule savedRrule = rruleRepository.save(rrule);
      List<BookingInput> bookings = buildRandomBookings(savedRrule);
      allSlots.addAll(slotGenerationService.generateSlotsForDay(
          current, List.of(savedRrule), bookings, caretaker));
      current = current.plusDays(1);
    }
    return allSlots;
  }

  private List<BookingInput> buildRandomBookings(CaretakerRRule rrule) {
    int count = rrule.getCapacity() / 2;
    List<BookingInput> bookings = new ArrayList<>();
    LocalTime from = rrule.getDtstart().toLocalTime();
    LocalTime to = rrule.getDtend().toLocalTime();
    int availableHours = to.getHour() - from.getHour();
    if (availableHours < 2 || count == 0) {
      return bookings;
    }
    for (int i = 0; i < count; i++) {
      int offset = i * 2;
      int duration = 1 + (i % 2);
      LocalTime start = from.plusHours(offset);
      LocalTime finish = start.plusHours(duration);
      if (finish.isAfter(to)) {
        finish = to;
      }
      bookings.add(new BookingInput(start, finish, rrule.getCapacity()));
    }
    return bookings;
  }

  private void recreateElasticIndex() {
    var indexOps = elasticsearchOperations.indexOps(ElasticSlotDocument.class);
    if (indexOps.exists()) {
      log.info("Deleting existing walking-slots index to refresh mappings");
      indexOps.delete();
    }
    indexOps.create();
    indexOps.putMapping(indexOps.createMapping());
  }
}
