package com.peti.backend.dto.caretaker;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Pricing configuration for a specific weight tier")
public record WeightTierConfig(
    @Schema(description = "Whether this weight tier is active", defaultValue = "true")
    boolean enabled,

    @Schema(description = "Pricing information for this weight tier")
    @NotNull(message = "Tier price must not be null")
    @Valid
    PriceInfo tierPrice
) {}

