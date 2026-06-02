package com.peti.backend.dto.caretaker;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import jakarta.annotation.Nullable;

@Schema(description = "Pricing details for a service or weight tier")
public record PriceInfo(
    @Schema(description = "Base price for the service", example = "50.00")
    @NotNull(message = "Price for service must not be null")
    @DecimalMin(value = "0.01", message = "Price for service must be greater than 0")
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal priceForService,

    @Schema(description = "Price per extending step (e.g. per extra 30 min)", example = "10.00")
    @NotNull(message = "Price per extending step must not be null")
    @DecimalMin(value = "0.00", message = "Price per extending step must not be negative")
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal pricePerExtendingStep,

    @Schema(description = "VIP price, null if not applicable", example = "80.00", nullable = true)
    @Nullable
    @DecimalMin(value = "0.01", message = "VIP price must be greater than 0 if provided")
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal vipPrice,

    @Schema(description = "Currency code (ISO 4217)", example = "UAH")
    @NotBlank(message = "Currency must not be blank")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO 4217 code")
    String currency,

    @Schema(description = "Optional pickup/delivery surcharge", example = "15.00", nullable = true)
    @Nullable
    @DecimalMin(value = "0.00", message = "Pickup price must not be negative")
    @JsonSerialize(using = ToStringSerializer.class)
    BigDecimal pickupPrice
) {}

