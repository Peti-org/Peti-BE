package com.peti.backend.dto.slot;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RequestCalendarSlots(
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    //including this day
    LocalDate fromDate,
    @NotNull(message = "Start time is required")
    @FutureOrPresent(message = "Date must be today or in the future")
    //including this day
    LocalDate toDate,
    @NotNull(message = "Caretaker id is needed")
    UUID caretakerId) {

}
