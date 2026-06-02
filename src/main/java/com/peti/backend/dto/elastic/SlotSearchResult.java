package com.peti.backend.dto.elastic;

import com.peti.backend.dto.caretaker.CaretakerPreferences;
import com.peti.backend.dto.caretaker.ServiceConfig;
import com.peti.backend.model.elastic.model.PriceBreakdown;
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

    // Caretaker preferences summary for display
    CaretakerPreferencesSummary preferences,

    // Calculated price for the requested parameters
    PriceBreakdown priceBreakdown
) {

  /**
   * Lightweight summary of a caretaker's preferences surfaced in search results.
   *
   * @param services       service types the caretaker offers (type names only)
   * @param maxPetsAtOnce  maximum pets handled simultaneously (from first matching service)
   */
  public record CaretakerPreferencesSummary(
      List<String> services,
      Integer maxPetsAtOnce
  ) {
    /** Build a summary from the full {@link CaretakerPreferences} object. */
    public static CaretakerPreferencesSummary from(CaretakerPreferences prefs) {
      if (prefs == null || prefs.services() == null || prefs.services().isEmpty()) {
        return new CaretakerPreferencesSummary(List.of(), null);
      }
      List<String> serviceNames = prefs.services().keySet().stream()
          .map(Enum::name)
          .toList();
      Integer maxPets = prefs.services().values().stream()
          .map(ServiceConfig::maxSimultaneousPets)
          .max(Integer::compareTo)
          .orElse(null);
      return new CaretakerPreferencesSummary(serviceNames, maxPets);
    }
  }
}
