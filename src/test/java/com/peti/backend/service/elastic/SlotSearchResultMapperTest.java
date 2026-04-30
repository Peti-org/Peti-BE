package com.peti.backend.service.elastic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.CaretakerPreferences.ServiceConfig;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.SortDirection;
import com.peti.backend.dto.elastic.ElasticSlotSearchRequest.SortField;
import com.peti.backend.dto.elastic.SlotSearchResult;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.model.elastic.model.PetInfo;
import com.peti.backend.model.elastic.model.PriceBreakdown;
import com.peti.backend.model.elastic.model.PriceCalculationResult;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SlotSearchResultMapperTest {

  @Mock
  private PriceCalculationService priceCalculationService;

  @InjectMocks
  private SlotSearchResultMapper mapper;

  private static final ServiceConfig CONFIG = new ServiceConfig(
      ServiceType.WALKING, false, true, false, 3,
      Duration.ofMinutes(60), Duration.ofMinutes(15), Duration.ofHours(2),
      Map.of(), List.of()
  );

  @Test
  @DisplayName("toSearchResult maps fields correctly with successful price")
  void toSearchResult_success() {
    ElasticSlotDocument slot = buildSlot("s1", 5, LocalTime.of(8, 0), LocalTime.of(12, 0));
    slot.setCaretakerPreferences(new CaretakerPreferences(List.of(CONFIG), Map.of()));
    PriceBreakdown breakdown = PriceBreakdown.builder()
        .basePrice(BigDecimal.valueOf(100)).total(BigDecimal.valueOf(130)).currency("UAH")
        .stepsCount(0).stepsPrice(BigDecimal.ZERO).extraPetsPrice(BigDecimal.ZERO)
        .subtotal(BigDecimal.valueOf(100)).durationMinutes(240).petsCount(1).build();
    when(priceCalculationService.calculatePrice(any(), anyList()))
        .thenReturn(PriceCalculationResult.success(breakdown));

    SlotSearchResult result = mapper.toSearchResult(slot, List.of(new PetInfo("dog", "medium")));

    assertThat(result.slotId()).isEqualTo("s1");
    assertThat(result.capacity()).isEqualTo(5);
    assertThat(result.caretakerId()).isEqualTo("ct-1");
    assertThat(result.caretakerFirstName()).isEqualTo("Іван");
    assertThat(result.priceBreakdown()).isNotNull();
    assertThat(result.priceBreakdown().total()).isEqualByComparingTo(BigDecimal.valueOf(130));
  }

  @Test
  @DisplayName("toSearchResult - failed price returns null breakdown")
  void toSearchResult_failedPrice() {
    ElasticSlotDocument slot = buildSlot("s1", 3, LocalTime.of(8, 0), LocalTime.of(12, 0));
    when(priceCalculationService.calculatePrice(any(), anyList()))
        .thenReturn(PriceCalculationResult.error("no config"));

    SlotSearchResult result = mapper.toSearchResult(slot, List.of(new PetInfo("dog", "medium")));
    assertThat(result.priceBreakdown()).isNull();
  }

  @Test
  @DisplayName("getComparator RATING ASC sorts by rating ascending")
  void comparator_ratingAsc() {
    ElasticSlotSearchRequest request = searchRequest(SortField.RATING, SortDirection.ASC);
    Comparator<SlotSearchResult> cmp = mapper.getComparator(request);

    SlotSearchResult r1 = resultWith(3, BigDecimal.valueOf(100));
    SlotSearchResult r2 = resultWith(5, BigDecimal.valueOf(50));
    assertThat(cmp.compare(r1, r2)).isNegative();
  }

  @Test
  @DisplayName("getComparator PRICE DESC sorts by price descending")
  void comparator_priceDesc() {
    ElasticSlotSearchRequest request = searchRequest(SortField.PRICE, SortDirection.DESC);
    Comparator<SlotSearchResult> cmp = mapper.getComparator(request);

    SlotSearchResult r1 = resultWith(3, BigDecimal.valueOf(100));
    SlotSearchResult r2 = resultWith(3, BigDecimal.valueOf(200));
    assertThat(cmp.compare(r1, r2)).isPositive(); // r2 (200) comes first in DESC
  }

  // ── helpers ───────────────────────────────────────────────────────────────

  private ElasticSlotDocument buildSlot(String id, int capacity, LocalTime from, LocalTime to) {
    return ElasticSlotDocument.builder()
        .id(id).caretakerId("ct-1").caretakerFirstName("Іван").caretakerLastName("Шевченко")
        .caretakerRating(5).caretakerCityId("1").caretakerCityName("Київ")
        .date(LocalDate.of(2026, 3, 1)).timeFrom(from).timeTo(to)
        .capacity(capacity).serviceConfig(CONFIG).build();
  }

  private SlotSearchResult resultWith(int rating, BigDecimal total) {
    PriceBreakdown bd = PriceBreakdown.builder()
        .basePrice(total).total(total).currency("UAH")
        .stepsCount(0).stepsPrice(BigDecimal.ZERO).extraPetsPrice(BigDecimal.ZERO)
        .subtotal(total).durationMinutes(60).petsCount(1).build();
    return new SlotSearchResult(
        "s", LocalDate.of(2026, 3, 1), LocalTime.of(8, 0), LocalTime.of(9, 0),
        3, 60, "ct", "N", "L", rating, "1", "K",
        SlotSearchResult.CaretakerPreferencesSummary.from(null), bd
    );
  }

  private ElasticSlotSearchRequest searchRequest(SortField field, SortDirection dir) {
    return new ElasticSlotSearchRequest(
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 7),
        null, null, "1", null, null, "UAH", null, null, null,
        0, 10, field, dir
    );
  }
}

