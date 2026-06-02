package com.peti.backend.dto.caretaker;

import com.peti.backend.model.internal.ServiceType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Schema(description = "Configuration for a single service type offered by a caretaker")
public record ServiceConfig(
    @Schema(description = "Type of the service")
    @NotNull(message = "Service type must not be null")
    ServiceType type,

    @Schema(description = "Whether this config is frozen (cannot be modified)")
    boolean frozen,

    @Schema(description = "Whether the price is negotiable")
    boolean priceNegotiable,

    @Schema(description = "Whether this is a VIP-tier offering")
    boolean isVip,

    @Schema(description = "Maximum pets handled simultaneously")
    @Min(value = 1, message = "Must handle at least 1 pet simultaneously")
    @Max(value = 20, message = "Cannot handle more than 20 pets simultaneously")
    int maxSimultaneousPets,

    @Schema(description = "Maximum people (owners) served simultaneously; defaults to 20 when absent")
    @Min(value = 1, message = "Must handle at least 1 person simultaneously")
    @Max(value = 20, message = "Cannot handle more than 20 people simultaneously")
    Integer maxSimultaneousPeople,

    @Schema(description = "Base duration (ISO-8601)", example = "PT1H")
    @NotNull(message = "Base duration must not be null")
    Duration baseDuration,

    @Schema(description = "Duration of each extending step (ISO-8601)", example = "PT30M")
    @NotNull(message = "Step duration must not be null")
    Duration stepDuration,

    @Schema(description = "Minimum advance notice before sales close (ISO-8601)", example = "PT2H")
    @NotNull(message = "Minimum advance booking duration must not be null")
    Duration minAdvanceMinutesBeforeSalesClose,

    //todo dog, cat and so on is not validated here, but we can add a custom validator if needed
    @Schema(description = "Per-pet-type configuration, keyed by pet type (e.g. Dog, Cat)")
    @Valid
    Map<String, PetConfig> configs,

    @Schema(description = "Tasks included in this service")
    @NotNull(message = "Tasks included list must not be null")
    @Size(max = 20, message = "Cannot have more than 20 included tasks")
    List<String> tasksIncluded
) {

  public ServiceConfig {
    if (configs == null) {
      configs = Map.of();
    }
    if (tasksIncluded == null) {
      tasksIncluded = List.of();
    }
    if (maxSimultaneousPeople == null) {
      maxSimultaneousPeople = 20;
    }
  }
}

