package com.peti.backend.service.elastic;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.SlotSearchResult;
import com.peti.backend.dto.elastic.SlotSearchResult.CaretakerPreferencesSummary;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.elastic.model.PetInfo;
import com.peti.backend.model.elastic.model.PriceBreakdown;
import com.peti.backend.model.elastic.model.PriceCalculationResult;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Maps {@link ElasticSlotDocument} instances to {@link SlotSearchResult} DTOs
 * and provides sort comparators for search results.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlotSearchResultMapper {

  private final PriceCalculationService priceCalculationService;

  /** Convert a slot document to a search result with a calculated price. */
  public SlotSearchResult toSearchResult(ElasticSlotDocument slot, List<PetInfo> pets) {
    PriceCalculationResult priceResult = priceCalculationService.calculatePrice(slot, pets);
    PriceBreakdown breakdown = priceResult.success() ? priceResult.breakdown() : null;

    if (!priceResult.success()) {
      log.debug("Price calculation failed for slot {}: {}", slot.getId(), priceResult.error());
    }

    return new SlotSearchResult(
        slot.getId(),
        slot.getDate(),
        slot.getTimeFrom(),
        slot.getTimeTo(),
        slot.getCapacity(),
        slot.getDurationMinutes(),
        slot.getCaretakerId(),
        slot.getCaretakerFirstName(),
        slot.getCaretakerLastName(),
        slot.getCaretakerRating(),
        slot.getCaretakerCityId(),
        slot.getCaretakerCityName(),
        buildPreferencesSummary(slot.getCaretakerPreferences()),
        breakdown
    );
  }

  /** Return a comparator matching the requested sort. */
  public Comparator<SlotSearchResult> getComparator(ElasticSlotSearchRequest request) {
    Comparator<SlotSearchResult> comparator = switch (request.sortBy()) {
      case RATING -> Comparator.comparing(
          SlotSearchResult::caretakerRating, Comparator.nullsLast(Comparator.naturalOrder()));
      case PRICE -> Comparator.comparing(
          r -> r.priceBreakdown() != null ? r.priceBreakdown().total() : null,
          Comparator.nullsLast(Comparator.naturalOrder()));
      case AVAILABILITY -> Comparator.comparing(
          SlotSearchResult::durationMinutes, Comparator.nullsLast(Comparator.naturalOrder()));
      case DATE -> Comparator.comparing(
          SlotSearchResult::date, Comparator.nullsLast(Comparator.naturalOrder()));
    };

    return request.sortDirection() == ElasticSlotSearchRequest.SortDirection.DESC
        ? comparator.reversed()
        : comparator;
  }

  private CaretakerPreferencesSummary buildPreferencesSummary(CaretakerPreferences prefs) {
    return CaretakerPreferencesSummary.from(prefs);
  }
}

