package com.peti.backend.dto.slot;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for admin slot generation endpoint.
 */
public record SlotGenerationRequest(
    SlotGenerationMode mode,
    LocalDate dateFrom,
    LocalDate dateTo,
    UUID caretakerId
) {

  public SlotGenerationMode modeOrDefault() {
    return mode != null ? mode : SlotGenerationMode.DEFAULT;
  }
}

