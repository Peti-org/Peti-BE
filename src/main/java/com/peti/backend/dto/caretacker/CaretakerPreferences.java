package com.peti.backend.dto.caretacker;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.peti.backend.model.internal.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@Schema(example = """
    {
      "services": [
        {
          "type": "WALKING",
          "frozen": false,
          "priceNegotiable": true,
          "isVip": false,
          "maxSimultaneousPets": 3,
          "baseDuration": "PT1H",
          "stepDuration": "PT30M",
          "minAdvanceMinutesBeforeSalesClose": "PT2H",
          "configs": {
            "Dog": {
              "enabled": true,
              "breeds": {
                "isIncluded": true,
                "include": ["Labrador", "Golden Retriever"],
                "exclude": []
              },
              "sameForAllWeights": false,
              "petPrice": null,
              "pickupDelivery": {
                "pickup": true,
                "bring": false
              },
              "weightTiers": {
                "Small": {
                  "tierPrice": {
                    "priceForService": 40.00,
                    "pricePerExtendingStep": 8.00,
                    "vipPrice": null,
                    "currency": "UAH",
                    "pickupPrice": null
                  },
                  "enabled": true
                },
                "Large": {
                  "tierPrice": {
                    "priceForService": 60.00,
                    "pricePerExtendingStep": 12.00,
                    "vipPrice": null,
                    "currency": "UAH",
                    "pickupPrice": null
                  },
                  "enabled": true
                }
              }
            }
          },
          "tasksIncluded": ["Feeding", "Brushing"]
        }
      ],
      "weeklySchedule": {
        "MONDAY": { "enabled": true, "from": "08:00", "to": "18:00" },
        "TUESDAY": { "enabled": true, "from": "09:00", "to": "17:00" },
        "WEDNESDAY": { "enabled": false, "from": "08:00", "to": "16:00" }
      }
    }
    """)
public record CaretakerPreferences(
    @Schema(description = "List of services the caretaker offers", example = "[]")
    @NotNull(message = "Services list must not be null")
    @Valid
    List<ServiceConfig> services,

    @Schema(description = "Weekly schedule keyed by day name (e.g. MONDAY, TUESDAY)", example = "{\"MONDAY\":{\"enabled\":true,\"from\":\"08:00\",\"to\":\"18:00\"},\"TUESDAY\":{\"enabled\":true,\"from\":\"09:00\",\"to\":\"17:00\"}}")
    @NotNull(message = "Weekly schedule must not be null")
    @Valid
    Map<String, DaySchedule> weeklySchedule
) {

  public CaretakerPreferences {
    if (services == null) {
      services = List.of();
    }
    if (weeklySchedule == null) {
      weeklySchedule = Map.of();
    }
  }

  public record PriceInfo(
      @Schema(description = "Base price for the service", defaultValue = "50.00", example = "50.00")
      @NotNull(message = "Price for service must not be null")
      @DecimalMin(value = "0.01", message = "Price for service must be greater than 0")
      @JsonSerialize(using = ToStringSerializer.class)
      BigDecimal priceForService,

      @Schema(description = "Price charged per extending step (e.g. per extra 30 min)", defaultValue = "10.00", example = "10.00")
      @NotNull(message = "Price per extending step must not be null")
      @DecimalMin(value = "0.00", message = "Price per extending step must not be negative")
      @JsonSerialize(using = ToStringSerializer.class)
      BigDecimal pricePerExtendingStep,

      @Schema(description = "Special VIP price, nullable if not applicable", defaultValue = "80.00", example = "80.00", nullable = true)
      @Nullable
      @DecimalMin(value = "0.01", message = "VIP price must be greater than 0 if provided")
      @JsonSerialize(using = ToStringSerializer.class)
      BigDecimal vipPrice,

      @Schema(description = "Currency code (ISO 4217)", defaultValue = "UAH", example = "UAH")
      @NotBlank(message = "Currency must not be blank")
      @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO 4217 code")
      String currency,

      @Schema(description = "Optional pickup/delivery surcharge", defaultValue = "15.00", example = "15.00", nullable = true)
      @Nullable
      @DecimalMin(value = "0.00", message = "Pickup price must not be negative")
      @JsonSerialize(using = ToStringSerializer.class)
      BigDecimal pickupPrice
  ) {}

  public record ServiceConfig(

      @Schema(description = "Type of the service", defaultValue = "WALKING", example = "WALKING", allowableValues = {"WALKING", "SITTING", "TRAINING", "GROOMING", "VET", "UNDEFINED"})
      @NotNull(message = "Service type must not be null")
      ServiceType type,

      @Schema(description = "Whether this service configuration is frozen and cannot be modified", defaultValue = "false", example = "false")
      boolean frozen,

      @Schema(description = "Whether the price is negotiable with the pet owner", defaultValue = "true", example = "true")
      boolean priceNegotiable,

      @Schema(description = "Whether this service is a VIP-tier offering", defaultValue = "false", example = "false")
      boolean isVip,

      @Schema(description = "Maximum number of pets that can be handled simultaneously", defaultValue = "3", example = "3")
      @Min(value = 1, message = "Must handle at least 1 pet simultaneously")
      @Max(value = 20, message = "Cannot handle more than 20 pets simultaneously")
      int maxSimultaneousPets,

      @Schema(description = "Base duration of the service in ISO-8601 duration format", defaultValue = "PT1H", example = "PT1H")
      @NotNull(message = "Base duration must not be null")
      Duration baseDuration,

      @Schema(description = "Duration of each extending step in ISO-8601 format", defaultValue = "PT30M", example = "PT30M")
      @NotNull(message = "Step duration must not be null")
      Duration stepDuration,

      @Schema(description = "Minimum advance notice required before sales close, in ISO-8601 duration format", defaultValue = "PT2H", example = "PT2H")
      @NotNull(message = "Minimum advance booking duration must not be null")
      Duration minAdvanceMinutesBeforeSalesClose,

      @Schema(description = "Per-pet-type configuration map, keyed by pet type name (e.g. Dog, Cat)", example = "{\"Dog\":{\"enabled\":true,\"breeds\":{\"isIncluded\":true,\"include\":[\"Labrador\"],\"exclude\":[]},\"sameForAllWeights\":false,\"petPrice\":null,\"pickupDelivery\":{\"pickup\":true,\"bring\":false},\"weightTiers\":{\"Small\":{\"tierPrice\":{\"priceForService\":40.00,\"pricePerExtendingStep\":8.00,\"vipPrice\":null,\"currency\":\"UAH\",\"pickupPrice\":null},\"enabled\":true}}}}")
      @Valid
      Map<String, PetConfig> configs,

      @Schema(description = "List of tasks included in this service", example = "[\"Feeding\", \"Brushing\"]")
      @NotNull(message = "Tasks included list must not be null")
      @Size(max = 20, message = "Cannot have more than 20 included tasks")
      List<String> tasksIncluded
  ) {}

  public record PetConfig(
      @Schema(description = "Whether this pet type is enabled for the service", defaultValue = "true", example = "true")
      boolean enabled,

      @Schema(description = "Breed inclusion/exclusion configuration")
      @NotNull(message = "Breed config must not be null")
      @Valid
      BreedConfig breeds,

      @Schema(description = "Whether the same pricing applies to all weight categories", defaultValue = "false", example = "false")
      boolean sameForAllWeights,

      @Schema(description = "Optional pet-specific price override, null means base price applies", nullable = true)
      @Nullable
      @Valid
      PriceInfo petPrice,

      @Schema(description = "Pickup and delivery options for this pet type")
      @NotNull(message = "Pickup/delivery config must not be null")
      @Valid
      PickupDelivery pickupDelivery,

      @Schema(description = "Weight tier pricing map, keyed by tier name (e.g. Small, Medium, Large)", example = "{\"Small\":{\"tierPrice\":{\"priceForService\":40.00,\"pricePerExtendingStep\":8.00,\"vipPrice\":null,\"currency\":\"UAH\",\"pickupPrice\":null},\"enabled\":true},\"Large\":{\"tierPrice\":{\"priceForService\":60.00,\"pricePerExtendingStep\":12.00,\"vipPrice\":null,\"currency\":\"UAH\",\"pickupPrice\":null},\"enabled\":true}}")
      @Nullable
      @Valid
      Map<String, WeightTier> weightTiers
  ) {}

  public record BreedConfig(
      @Schema(description = "True means the listed breeds are included; false means they are excluded", defaultValue = "true", example = "true")
      boolean isIncluded,

      @Schema(description = "List of breed names to include", example = "[\"Labrador\", \"Golden Retriever\"]")
      @NotNull(message = "Include list must not be null")
      @Size(max = 100, message = "Cannot specify more than 100 included breeds")
      List<String> include,

      @Schema(description = "List of breed names to exclude", example = "[]")
      @NotNull(message = "Exclude list must not be null")
      @Size(max = 100, message = "Cannot specify more than 100 excluded breeds")
      List<String> exclude
  ) {}

  public record PickupDelivery(
      @Schema(description = "Whether the caretaker can pick up the pet from the owner", defaultValue = "true", example = "true")
      boolean pickup,

      @Schema(description = "Whether the owner must bring the pet to the caretaker", defaultValue = "false", example = "false")
      boolean bring
  ) {}

  public record WeightTier(
      @Schema(description = "Pricing information specific to this weight tier")
      @NotNull(message = "Tier price must not be null")
      @Valid
      PriceInfo tierPrice,

      @Schema(description = "Whether this weight tier is active", defaultValue = "true", example = "true")
      boolean enabled
  ) {}

  public record DaySchedule(
      @Schema(description = "Whether the caretaker is available on this day", defaultValue = "true", example = "true")
      boolean enabled,

      @Schema(description = "Start time of availability for this day (HH:mm)", defaultValue = "08:00", example = "08:00")
      @NotNull(message = "Schedule start time must not be null")
      String from,

      @Schema(description = "End time of availability for this day (HH:mm)", defaultValue = "18:00", example = "18:00")
      @NotNull(message = "Schedule end time must not be null")
      String to
  ) {
    private static final java.time.format.DateTimeFormatter TIME_FORMAT =
        java.time.format.DateTimeFormatter.ofPattern("HH:mm");

    public DaySchedule(boolean enabled, LocalTime from, LocalTime to) {
      this(enabled, from.format(TIME_FORMAT), to.format(TIME_FORMAT));
    }

    public LocalTime fromTime() {
      return LocalTime.parse(from, TIME_FORMAT);
    }

    public LocalTime toTime() {
      return LocalTime.parse(to, TIME_FORMAT);
    }
  }
}
