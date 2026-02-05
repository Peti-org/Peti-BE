package com.peti.backend.model.elastic;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "walking-slots")
public class ElasticSlotDocument { // Needs changes for sure

  @Id
  private String id;

  @Version
  private Long version;

  // Caretaker (service provider) information - denormalized for efficient searching
  @Field(type = FieldType.Keyword)
  private String caretakerId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerFirstName;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerLastName;

  @Field(type = FieldType.Integer)
  private Integer caretakerRating;

  @Field(type = FieldType.Keyword)
  private String caretakerCityId;

  @Field(type = FieldType.Text, analyzer = "standard")
  private String caretakerCityName;

  // Caretaker preferences for pet matching
  @Field(type = FieldType.Nested)
  private CaretakerPreferences caretakerPreferences;

  // Slot time range
  @Field(type = FieldType.Date, format = DateFormat.date)
  private LocalDate date;

  @Field(type = FieldType.Date, format = DateFormat.hour_minute_second)
  private LocalTime timeFrom;

  @Field(type = FieldType.Date, format = DateFormat.hour_minute_second)
  private LocalTime timeTo;

  // Pricing configuration - all parameters for price calculation
  @Field(type = FieldType.Nested)
  private PricingConfig pricingConfig;

  // Capacity management
  @Field(type = FieldType.Integer)
  private Integer capacity;

  // Metadata
  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant createdAt;

  @Field(type = FieldType.Date, format = DateFormat.date_time)
  private Instant updatedAt;

  // For optimistic locking and consistency tracking
  @Field(type = FieldType.Long)
  private Long sequenceNumber;

  // Reference to original RRULE for slot generation
  @Field(type = FieldType.Keyword)
  private String rruleId;

  /**
   * Nested object for caretaker's pet preferences.
   * Used for matching user's pets with caretaker capabilities.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CaretakerPreferences { // Типу характеристики по кожній тварині з її цінами

    // Accepted animal types (dog, cat, bird, etc.)
    @Field(type = FieldType.Keyword)
    private List<String> acceptedAnimalTypes;

    // Accepted breeds (can be "all" or specific breed IDs)
    @Field(type = FieldType.Keyword)
    private List<String> acceptedBreedIds;

    // Size constraints
    @Field(type = FieldType.Keyword)
    private List<String> acceptedSizes; // small, medium, large, extra_large

    // Max weight in kg
    @Field(type = FieldType.Double)
    private Double maxWeightKg;

    // Max number of pets at once
    @Field(type = FieldType.Integer)
    private Integer maxPetsAtOnce;

    // Special requirements the caretaker can handle
    @Field(type = FieldType.Keyword)
    private List<String> specialRequirements; // medication, special_diet, elderly_care, puppy_care

    // Whether caretaker has outdoor space
    @Field(type = FieldType.Boolean)
    private Boolean hasOutdoorSpace;

    // Whether caretaker accepts pets with special needs
    @Field(type = FieldType.Boolean)
    private Boolean acceptsSpecialNeeds;
  }

  /**
   * Pricing configuration for the caretaker.
   * Contains all parameters needed for price calculation.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class PricingConfig { // можливо треба по полях розкинути на тварин, без цього класу

    // Minimum slot duration in minutes (e.g., 60)
    @Field(type = FieldType.Integer)
    private Integer minDurationMinutes;

    // Step size in minutes for extending slot (10, 15, 20, 30)
    @Field(type = FieldType.Integer)
    private Integer stepMinutes;

    // Base price for minimum duration
    @Field(type = FieldType.Double)
    private BigDecimal basePricePerMinDuration;

    // Price for each additional step
    @Field(type = FieldType.Double)
    private BigDecimal pricePerStep;

    // Extra pet prices by animal type and weight category
    @Field(type = FieldType.Nested)
    private List<ExtraPetPrice> extraPetPrices;

    // Provider's tax rate (e.g., 0.20 for +20%)
    // Applied before service fee
    @Field(type = FieldType.Double)
    private BigDecimal providerTaxRate;

    // Currency code
    @Field(type = FieldType.Keyword)
    private String currency;
  }

  /**
   * Extra price for additional pets by type and weight.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ExtraPetPrice { // Має бути в ціні

    @Field(type = FieldType.Keyword)
    private String animalType; // dog, cat, bird, etc.

    @Field(type = FieldType.Keyword)
    private String weightCategory; // small, medium, large, extra_large

    @Field(type = FieldType.Double)
    private BigDecimal price;
  }

  /**
   * Check if slot has enough capacity for requested number of pets.
   */
  public boolean hasCapacityFor(int petsCount) {
    return capacity != null && capacity >= petsCount;
  }

  /**
   * Calculate slot duration in minutes.
   */
  public long getDurationMinutes() {
    if (timeFrom == null || timeTo == null) {
      return 0;
    }
    return java.time.Duration.between(timeFrom, timeTo).toMinutes();
  }
}
