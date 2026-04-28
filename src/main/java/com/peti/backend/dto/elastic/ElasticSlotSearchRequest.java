package com.peti.backend.dto.elastic;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Request DTO for searching slots in Elasticsearch.
 * Supports filtering by various criteria and pet matching.
 */
public record ElasticSlotSearchRequest(
    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate dateFrom,

    @FutureOrPresent(message = "End date must be today or in the future")
    @NotNull(message = "End date is required")
    LocalDate dateTo,

    // Time range - optional, defaults to full day
    LocalTime timeFrom,
    LocalTime timeTo,

    @NotNull(message = "City ID is required")
    String cityId,

    // Price range - ше питання, як ми будемо рахувати ціну, бо вона залежить від тварин і їх кількості і бла-бла-бла, що еластік не може сам порахувати, а видавати юзеру якусь фігню не прикольно
    @Positive(message = "Min price must be positive")
    BigDecimal minPricePerHour,//todo add calculation service on top of standart price

    @Positive(message = "Max price must be positive")
    BigDecimal maxPricePerHour,

    @NotNull(message = "Price currency is required")
    String currency,

    // Minimum caretaker rating
    @Min(value = 1, message = "Min rating must be at least 1")
    @Max(value = 5, message = "Max rating is 5")
    Integer minRating,

    // Pet matching - user's pets that need to be matched with caretaker preferences
    List<PetFilter> pets,

    // Matching mode for pets
    PetMatchMode petMatchMode,

    // Pagination
    @Min(value = 0, message = "Page must be non-negative")
    Integer page,//todo rewrite pagination

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size cannot exceed 100")
    Integer pageSize,

    SortField sortBy,
    SortDirection sortDirection
) {
  /**
   * Filter criteria for a single pet.
   */
  public record PetFilter(
      String animalType,    // dog, cat, bird, etc.
      String breedId,       // specific breed or "mixed"
      String size,          // small, medium, large, extra_large
      Double weightKg, // треба буде подумати щодо кілограмів і розмірів. Різні тварини, вочевидь, мають різні вагові категорії, коти легші за собак в середньому
      Boolean hasSpecialNeeds,
      List<String> specialRequirements  // medication, special_diet, etc.
  ) {}

  //for now only all any for future
  public enum PetMatchMode {// Найс штука. Може бути, що нема людей для обох твоїх тварин, то доведеться кому віддати одного, а комусь іншого
    ALL,      // Caretaker must accept ALL pets
    ANY       // Caretaker must accept at least ONE pet
  }//todo check, seems not usefull at all?? is real case when customer wnat to do that

  public enum SortField {
    RATING,
    PRICE,
    AVAILABILITY,
    DATE
  }

  public enum SortDirection {
    ASC,
    DESC
  }

  /**
   * Apply default values.
   */
  public ElasticSlotSearchRequest withDefaults() {
    return new ElasticSlotSearchRequest(
        dateFrom,
        dateTo != null ? dateTo : dateFrom.plusDays(7),
        timeFrom != null ? timeFrom : LocalTime.of(8, 0),
        timeTo != null ? timeTo : LocalTime.of(20, 0),
        cityId,
        minPricePerHour,
        maxPricePerHour,
        currency,
        minRating,
        pets,
        petMatchMode != null ? petMatchMode : PetMatchMode.ALL,
        page != null ? page : 0,
        pageSize != null ? pageSize : 20,
        sortBy != null ? sortBy : SortField.RATING,
        sortDirection != null ? sortDirection : SortDirection.DESC
    );
  }
}
