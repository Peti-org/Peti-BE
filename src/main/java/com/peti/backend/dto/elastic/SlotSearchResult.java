package com.peti.backend.dto.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.DaySchedule;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.model.elastic.model.PriceBreakdown;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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
   * @param weeklySchedule caretaker's weekly availability schedule
   * @param maxPetsAtOnce  maximum pets handled simultaneously (from first matching service)
   */
  public record CaretakerPreferencesSummary(
      List<String> services,
      Map<String, DaySchedule> weeklySchedule,
      Integer maxPetsAtOnce
  ) {
    /** Build a summary from the full {@link CaretakerPreferences} object. */
    public static CaretakerPreferencesSummary from(CaretakerPreferences prefs) {
      if (prefs == null) {
        return new CaretakerPreferencesSummary(List.of(), Map.of(), null);
      }
      List<String> serviceNames = prefs.services() == null ? List.of() :
          prefs.services().stream()
              .filter(s -> s.type() != null)
              .map(s -> s.type().name())
              .toList();
      Integer maxPets = prefs.services() == null || prefs.services().isEmpty() ? null :
          prefs.services().stream()
              .map(ServiceConfig::maxSimultaneousPets)
              .max(Integer::compareTo)
              .orElse(null);
      return new CaretakerPreferencesSummary(
          serviceNames,
          prefs.weeklySchedule() != null ? prefs.weeklySchedule() : Map.of(),
          maxPets
      );
    }
  }
}
