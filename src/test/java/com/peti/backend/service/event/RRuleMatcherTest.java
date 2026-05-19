package com.peti.backend.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.exception.BadRequestException;
import com.peti.backend.repository.CaretakerRRuleRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RRuleMatcherTest {

  private static final UUID CARETAKER_ID =
      UUID.fromString("b1a7e8e2-8c2e-4c1a-9e2a-123456789abc");

  @Mock private CaretakerRRuleRepository rruleRepository;
  @InjectMocks private RRuleMatcher rruleMatcher;

  @Test
  @DisplayName("findMatchingRules - returns rules overlapping the time window")
  void findMatchingRules_overlapping() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(4)); // 08:00-12:00
    when(rruleRepository.findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(
        CARETAKER_ID, "WALKING")).thenReturn(List.of(rule));

    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 10, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 11, 0);

    List<CaretakerRRule> result = rruleMatcher.findMatchingRules(
        CARETAKER_ID, "WALKING", from, to);

    assertThat(result).containsExactly(rule);
  }

  @Test
  @DisplayName("findMatchingRules - no overlap throws BadRequestException")
  void findMatchingRules_noOverlap() {
    CaretakerRRule rule = buildRule(LocalTime.of(8, 0), Duration.ofHours(2)); // 08:00-10:00
    when(rruleRepository.findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(
        CARETAKER_ID, "WALKING")).thenReturn(List.of(rule));

    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 14, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 15, 0);

    assertThatThrownBy(() -> rruleMatcher.findMatchingRules(
        CARETAKER_ID, "WALKING", from, to))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("No available schedule");
  }

  @Test
  @DisplayName("findMatchingRules - no candidates throws BadRequestException")
  void findMatchingRules_noCandidates() {
    when(rruleRepository.findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(
        CARETAKER_ID, "SITTING")).thenReturn(List.of());

    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 10, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 11, 0);

    assertThatThrownBy(() -> rruleMatcher.findMatchingRules(
        CARETAKER_ID, "SITTING", from, to))
        .isInstanceOf(BadRequestException.class);
  }

  @Test
  @DisplayName("findMatchingRules - multiple rules, only overlapping ones returned")
  void findMatchingRules_multipleRules() {
    CaretakerRRule morning = buildRule(LocalTime.of(8, 0), Duration.ofHours(4)); // 08-12
    CaretakerRRule afternoon = buildRule(LocalTime.of(14, 0), Duration.ofHours(4)); // 14-18
    when(rruleRepository.findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(
        CARETAKER_ID, "WALKING")).thenReturn(List.of(morning, afternoon));

    LocalDateTime from = LocalDateTime.of(2026, 5, 2, 10, 0);
    LocalDateTime to = LocalDateTime.of(2026, 5, 2, 11, 0);

    List<CaretakerRRule> result = rruleMatcher.findMatchingRules(
        CARETAKER_ID, "WALKING", from, to);

    assertThat(result).containsExactly(morning);
  }

  private CaretakerRRule buildRule(LocalTime start, Duration duration) {
    CaretakerRRule rule = new CaretakerRRule();
    rule.setRruleId(UUID.randomUUID());
    rule.setSlotStartTime(start);
    rule.setSlotDuration(duration);
    rule.setIsEnabled(true);
    return rule;
  }
}

