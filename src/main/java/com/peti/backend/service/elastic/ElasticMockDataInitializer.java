package com.peti.backend.service.elastic;

import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.ElasticSlotDocument.CaretakerPreferences;
import com.peti.backend.model.elastic.ElasticSlotDocument.ExtraPetPrice;
import com.peti.backend.model.elastic.ElasticSlotDocument.PricingConfig;
import com.peti.backend.repository.elastic.ElasticSlotRepository;
import com.peti.backend.service.elastic.SlotGenerationService.BookingInput;
import com.peti.backend.service.elastic.SlotGenerationService.CaretakerInput;
import com.peti.backend.service.elastic.SlotGenerationService.RRuleInput;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Service for initializing mock slot data in Elasticsearch.
 * Uses the new capacity-layered slot generation approach.
 * Active only when elasticsearch.mock-data.enabled=true.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticMockDataInitializer {

  private final ElasticSlotRepository slotRepository;
  private final SlotGenerationService slotGenerationService;

  @Value("${elasticsearch.mock-data.enabled:true}")
  private boolean mockDataEnabled;

  @Value("${elasticsearch.mock-data.caretakers:50}")
  private int numberOfCaretakers;

  @Value("${elasticsearch.mock-data.days-ahead:60}")
  private int daysAhead;

  private final Random random = new Random(42); // Fixed seed for reproducible data

  // Ukrainian first names
  private final String[] firstNames = {
      "Олександр", "Марія", "Дмитро", "Анна", "Іван",
      "Олена", "Микола", "Тетяна", "Андрій", "Юлія",
      "Тарас", "Максим", "Федір", "Ростислав"
  };

  // Ukrainian last names
  private final String[] lastNames = {
      "Шевченко", "Коваленко", "Бондаренко", "Ткаченко", "Мельник",
      "Кравченко", "Олійник", "Лисенко", "Морозенко", "Петренко",
      "Лщшн", "Зварич", "Орищак"
  };

  // Cities
  private final String[][] cities = {
      {"1", "Київ"},
      {"2", "Львів"},
      {"3", "Дніпро"},
      {"4", "Харків"},
      {"5", "Одеса"},
  };

  @EventListener(ApplicationReadyEvent.class)
  public void initializeMockData() {
    if (!mockDataEnabled) {
      log.info("Elasticsearch mock data initialization is disabled");
      return;
    }

    // Check if data already exists
    long existingSlots = slotRepository.count();
    if (existingSlots > 0) {
      log.info("Elasticsearch already has {} slots, skipping mock data initialization", existingSlots);
      return;
    }

    log.info("Initializing Elasticsearch mock data with {} caretakers for {} days ahead",
        numberOfCaretakers, daysAhead);

    List<MockCaretaker> caretakers = generateMockCaretakers();

    int totalSlots = 0;
    for (MockCaretaker caretaker : caretakers) {
      List<ElasticSlotDocument> slots = generateSlotsForCaretaker(caretaker);
      slotRepository.saveAll(slots);
      totalSlots += slots.size();
      log.debug("Created {} slots for caretaker {}", slots.size(), caretaker.id);
    }

    log.info("Mock data initialization completed. Created {} slots for {} caretakers.",
        totalSlots, caretakers.size());
  }

  private List<MockCaretaker> generateMockCaretakers() {
    List<MockCaretaker> caretakers = new ArrayList<>();

    for (int i = 0; i < numberOfCaretakers; i++) {
      String[] city = cities[i % cities.length];

      CaretakerPreferences preferences = CaretakerPreferences.builder()
          .acceptedAnimalTypes(randomAnimalTypes())
          .acceptedBreedIds(List.of("all"))
          .acceptedSizes(randomSizes())
          .maxWeightKg(20.0 + random.nextDouble() * 40) // 20-60 kg
          .maxPetsAtOnce(2 + random.nextInt(4)) // 2-5 pets
          .specialRequirements(randomSpecialRequirements())
          .hasOutdoorSpace(random.nextBoolean())
          .acceptsSpecialNeeds(random.nextBoolean())
          .build();

      caretakers.add(new MockCaretaker(
          UUID.randomUUID().toString(),
          firstNames[random.nextInt(firstNames.length)],
          lastNames[random.nextInt(lastNames.length)],
          3 + random.nextInt(3), // Rating 3-5
          city[0],
          city[1],
          preferences
      ));
    }

    return caretakers;
  }

  private List<String> randomAnimalTypes() {
    List<String> types = new ArrayList<>();
    types.add("dog");
    if (random.nextBoolean()) types.add("cat");
    if (random.nextInt(4) == 0) types.add("bird");
    return types;
  }

  private List<String> randomSizes() {
    List<String> sizes = new ArrayList<>();
    sizes.add("small");
    sizes.add("medium");
    if (random.nextBoolean()) sizes.add("large");
    if (random.nextInt(3) == 0) sizes.add("extra_large");
    return sizes;
  }

  private List<String> randomSpecialRequirements() {
    List<String> reqs = new ArrayList<>();
    if (random.nextBoolean()) reqs.add("medication");
    if (random.nextInt(3) == 0) reqs.add("special_diet");
    if (random.nextInt(4) == 0) reqs.add("elderly_care");
    return reqs;
  }

  private List<ElasticSlotDocument> generateSlotsForCaretaker(MockCaretaker caretaker) {
    List<ElasticSlotDocument> allSlots = new ArrayList<>();
    LocalDate startDate = LocalDate.now();
    LocalDate endDate = startDate.plusDays(daysAhead);

    // Create caretaker input for slot generation
    CaretakerInput caretakerInput = new CaretakerInput(
        caretaker.id,
        caretaker.firstName,
        caretaker.lastName,
        caretaker.rating,
        caretaker.cityId,
        caretaker.cityName,
        caretaker.preferences
    );

    LocalDate currentDate = startDate;
    while (!currentDate.isAfter(endDate)) {
      DayOfWeek dow = currentDate.getDayOfWeek();

      // Skip Sundays mostly
      if (dow == DayOfWeek.SUNDAY && random.nextBoolean()) {
        currentDate = currentDate.plusDays(1);
        continue;
      }

      // Generate RRule for this day (simulating caretaker's availability)
      int startHour = 8 + random.nextInt(2); // 8 or 9
      int endHour = 18 + random.nextInt(4);  // 18-21
      int maxCapacity = 2 + random.nextInt(3); // 2-4
      
      // Generate pricing config for this caretaker
      PricingConfig pricingConfig = generatePricingConfig();
      
      RRuleInput rrule = new RRuleInput(
          LocalTime.of(startHour, 0),
          LocalTime.of(endHour, 0),
          maxCapacity,
          pricingConfig
      );

      // Generate random bookings (0-2 bookings per day)
      List<BookingInput> bookings = generateRandomBookings(rrule, random.nextInt(3));

      // Generate capacity-layered slots using the new service
      // Wrap single RRule in a list for the new API
      List<ElasticSlotDocument> daySlots = slotGenerationService.generateSlotsForDay(
          currentDate, List.of(rrule), bookings, caretakerInput
      );

      allSlots.addAll(daySlots);
      currentDate = currentDate.plusDays(1);
    }

    return allSlots;
  }

  private List<BookingInput> generateRandomBookings(RRuleInput rrule, int count) {
    List<BookingInput> bookings = new ArrayList<>();
    
    int availableHours = rrule.timeTo().getHour() - rrule.timeFrom().getHour();
    if (availableHours < 2 || count == 0) {
      return bookings;
    }

    for (int i = 0; i < count; i++) {
      int bookingStartOffset = random.nextInt(availableHours - 1);
      int bookingDuration = 1 + random.nextInt(Math.min(3, availableHours - bookingStartOffset));
      int bookedCapacity = 1 + random.nextInt(Math.max(1, rrule.maxCapacity() - 1));

      LocalTime bookingStart = rrule.timeFrom().plusHours(bookingStartOffset);
      LocalTime bookingEnd = bookingStart.plusHours(bookingDuration);
      
      if (bookingEnd.isAfter(rrule.timeTo())) {
        bookingEnd = rrule.timeTo();
      }

      bookings.add(new BookingInput(bookingStart, bookingEnd, bookedCapacity));
    }

    return bookings;
  }

  /**
   * Generate realistic pricing configuration for a caretaker.
   */
  private PricingConfig generatePricingConfig() {
    // Step options: 10, 15, 20, 30 minutes
    int[] stepOptions = {10, 15, 20, 30};
    int stepMinutes = stepOptions[random.nextInt(stepOptions.length)];
    
    // Min duration must be divisible by step: 30, 45, 60, 90, 120 minutes
    int[] minDurationMultipliers = {2, 3, 4, 6}; // multiplied by step
    int minDuration = stepMinutes * minDurationMultipliers[random.nextInt(minDurationMultipliers.length)];
    
    // Base price 80-200 UAH
    BigDecimal basePrice = BigDecimal.valueOf(80 + random.nextInt(121));
    
    // Price per step 20-60 UAH
    BigDecimal pricePerStep = BigDecimal.valueOf(20 + random.nextInt(41));
    
    // Generate extra pet prices for different animal types and weights
    List<ExtraPetPrice> extraPetPrices = generateExtraPetPrices();
    
    // Some caretakers add tax (0, 10%, 20%)
    BigDecimal providerTaxRate = null;
    int taxChoice = random.nextInt(4);
    if (taxChoice == 1) {
      providerTaxRate = BigDecimal.valueOf(0.10);
    } else if (taxChoice == 2) {
      providerTaxRate = BigDecimal.valueOf(0.20);
    }
    
    return PricingConfig.builder()
        .minDurationMinutes(minDuration)
        .stepMinutes(stepMinutes)
        .basePricePerMinDuration(basePrice)
        .pricePerStep(pricePerStep)
        .extraPetPrices(extraPetPrices)
        .providerTaxRate(providerTaxRate)
        .currency("UAH")
        .build();
  }

  /**
   * Generate extra pet prices for various animal types and weight categories.
   */
  private List<ExtraPetPrice> generateExtraPetPrices() {
    List<ExtraPetPrice> prices = new ArrayList<>();
    
    String[] animalTypes = {"dog", "cat", "bird"};
    String[] weightCategories = {"small", "medium", "large"};
    
    // Base prices by animal type
    int[] basePricesByType = {80, 60, 40}; // dog most expensive, bird cheapest
    
    for (int t = 0; t < animalTypes.length; t++) {
      for (String weight : weightCategories) {
        // Larger animals cost more
        int weightMultiplier = switch (weight) {
          case "small" -> 1;
          case "medium" -> 2;
          case "large" -> 3;
          default -> 1;
        };
        
        int price = basePricesByType[t] + (weightMultiplier - 1) * 20 + random.nextInt(20);
        
        prices.add(ExtraPetPrice.builder()
            .animalType(animalTypes[t])
            .weightCategory(weight)
            .price(BigDecimal.valueOf(price))
            .build());
      }
    }
    
    return prices;
  }

  record MockCaretaker(
      String id,
      String firstName,
      String lastName,
      int rating,
      String cityId,
      String cityName,
      CaretakerPreferences preferences
  ) {}
}
