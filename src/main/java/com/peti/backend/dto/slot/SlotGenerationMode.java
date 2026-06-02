package com.peti.backend.dto.slot;

/**
 * Mode for slot generation.
 */
public enum SlotGenerationMode {
  /**
   * Incremental — generates only from caretaker.generatedTo to now + daysAhead.
   */
  DEFAULT,
  /**
   * Force — regenerates the full range from now to now + daysAhead.
   */
  FORCE
}

