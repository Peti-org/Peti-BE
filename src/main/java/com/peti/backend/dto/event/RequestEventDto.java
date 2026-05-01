package com.peti.backend.dto.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create an event from a CaretakerRRule for a time window")
public record RequestEventDto(
    @NotNull(message = "rruleId must not be null")
    @Schema(description = "ID of the CaretakerRRule the event is booked from")
    UUID rruleId,

    @NotNull(message = "datetimeFrom must not be null")
    @Schema(description = "Event start (inclusive). Must fall inside the RRule's date range.")
    LocalDateTime datetimeFrom,

    @NotNull(message = "datetimeTo must not be null")
    @Schema(description = "Event end (exclusive). Must be strictly after datetimeFrom.")
    LocalDateTime datetimeTo,

    @NotEmpty(message = "Pets list must not be empty")
    @Schema(description = "Pet IDs participating in the event (must belong to current user)")
    List<UUID> petsIds) {
}

