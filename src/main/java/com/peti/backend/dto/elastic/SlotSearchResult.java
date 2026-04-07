package com.peti.backend.dto.elastic;

import com.peti.backend.service.elastic.PriceCalculationService.PriceBreakdown;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Response for slot search results.
 * Contains slot information with calculated price.
 */
public record SlotSearchResult(
    // Slot info
    String slotId,
    LocalDate date,
    LocalTime timeFrom,
    LocalTime timeTo,
    Integer capacity,
    long durationMinutes,
    
    // Caretaker info
    String caretakerId,
    String caretakerFirstName,
    String caretakerLastName,
    Integer caretakerRating,
    String caretakerCityId,
    String caretakerCityName,
    
    // Caretaker preferences summary
    CaretakerPreferencesSummary preferences,
    
    // Calculated price for requested parameters
    PriceBreakdown priceBreakdown
) {
  
  /**
   * Summary of caretaker's pet preferences for display.
   */
  public record CaretakerPreferencesSummary(
      List<String> acceptedAnimalTypes,
      List<String> acceptedSizes,
      Double maxWeightKg,
      Integer maxPetsAtOnce,
      Boolean hasOutdoorSpace,
      Boolean acceptsSpecialNeeds
  ) {}
}
