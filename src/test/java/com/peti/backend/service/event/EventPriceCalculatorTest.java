package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.PriceDto;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.domain.Pet;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventPriceCalculatorTest {

  private final EventPriceCalculator calculator = new EventPriceCalculator();

  @Test
  @DisplayName("calculate - two Dog pets, each gets petPrice.priceForService=100, total=200")
  void calculate_twoPets() {
    CaretakerRRule rrule = ResourceLoader.loadResource(
        "rrule-for-event-entity.json", CaretakerRRule.class);
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    PriceDto price = calculator.calculate(rrule.getCaretaker(), "WALKING", pets);

    assertThat(price.totalPrice()).isEqualByComparingTo("200.00");
    assertThat(price.currency()).isEqualTo("UAH");
    assertThat(price.priceBreakdown()).hasSize(2);
    assertThat(price.priceBreakdown().get(0).type()).isEqualTo("BASE_PET_1");
    assertThat(price.priceBreakdown().get(0).price()).isEqualByComparingTo("100.00");
    assertThat(price.priceBreakdown().get(1).type()).isEqualTo("BASE_PET_2");
    assertThat(price.priceBreakdown().get(1).price()).isEqualByComparingTo("100.00");
  }

  @Test
  @DisplayName("calculate - single pet returns total = single pet price")
  void calculate_singlePet() {
    CaretakerRRule rrule = ResourceLoader.loadResource(
        "rrule-for-event-entity.json", CaretakerRRule.class);
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<List<Pet>>() {});

    PriceDto price = calculator.calculate(rrule.getCaretaker(), "WALKING", List.of(pets.get(0)));

    assertThat(price.totalPrice()).isEqualByComparingTo("100.00");
    assertThat(price.priceBreakdown()).hasSize(1);
    assertThat(price.priceBreakdown().get(0).type()).isEqualTo("BASE_PET_1");
  }

  @Test
  @DisplayName("calculate - null caretaker throws IllegalArgumentException")
  void calculate_nullCaretaker() {
    List<Pet> pets = ResourceLoader.loadResource(
        "pets-for-event.json", new TypeReference<>() {});

    assertThatThrownBy(() -> calculator.calculate(null, "WALKING", pets))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("calculate - empty pets list throws IllegalArgumentException")
  void calculate_emptyPets() {
    CaretakerRRule rrule = ResourceLoader.loadResource(
        "rrule-for-event-entity.json", CaretakerRRule.class);

    assertThatThrownBy(() -> calculator.calculate(rrule.getCaretaker(), "WALKING", List.of()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
