package com.peti.backend.dto.slot;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record RequestSlotDto(
    @NotNull(message = "Date must not be null")
    @FutureOrPresent(message = "Date must be today or in the future")
    LocalDate date,
    @NotNull(message = "Start time is required")
    LocalTime timeFrom,
    @NotNull(message = "End time is required")
    LocalTime timeTo,
    @NotEmpty(message = "Type must not be empty")
    String type,
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Price must have max 10 digits before decimal and 2 after")
    BigDecimal price) {

}
