package com.peti.backend.dto.slot;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

public record SlotFiltersDto(
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    Optional<LocalDate> onDate,
    @DecimalMin(value = "0.0", inclusive = false, message = "Min price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Min price must have max 10 digits before decimal and 2 after")
    Optional<BigDecimal> minPrice,
    @DecimalMin(value = "0.0", inclusive = false, message = "Max price must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Max price must have max 10 digits before decimal and 2 after")
    Optional<BigDecimal> maxPrice,
    @NotEmpty(message = "Type must not be empty")
    Optional<String> type,
    @NotNull(message = "Start time is required")
    Optional<LocalTime> timeFrom,
    @NotNull(message = "End time is required")
    Optional<LocalTime> timeTo) {

}
