package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CaretakerRRuleServiceTest {

  private CaretakerRRuleRepository rruleRepository;
  private EntityManager entityManager;
  private CaretakerRRuleService rruleService;

  @BeforeEach
  void setUp() {
    rruleRepository = mock(CaretakerRRuleRepository.class);
    entityManager = mock(EntityManager.class);
    rruleService = new CaretakerRRuleService(rruleRepository, entityManager);
  }

  @Test
  void testGetAllRRulesForCaretaker_ReturnsListOfRRules() {
    // Given
    Caretaker caretaker = ResourceLoader.loadResource("caretaker-entity.json", Caretaker.class);
    UUID caretakerId = caretaker.getCaretakerId();
    List<CaretakerRRule> rrules = ResourceLoader.loadResource("rrule-entities.json", new TypeReference<>() {});

    when(rruleRepository.findAllByCaretaker_CaretakerId(caretakerId)).thenReturn(rrules);

    // When
    List<RRuleDto> result = rruleService.getAllRRulesForCaretaker(caretakerId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(rrules.get(0).getRrule(), result.get(0).rrule());
    assertEquals(rrules.get(1).getRrule(), result.get(1).rrule());
    verify(rruleRepository).findAllByCaretaker_CaretakerId(caretakerId);
  }

  @Test
  void testGetAllRRulesForCaretaker_EmptyList() {
    // Given
    UUID caretakerId = UUID.randomUUID();
    when(rruleRepository.findAllByCaretaker_CaretakerId(caretakerId)).thenReturn(List.of());

    // When
    List<RRuleDto> result = rruleService.getAllRRulesForCaretaker(caretakerId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
    verify(rruleRepository).findAllByCaretaker_CaretakerId(caretakerId);
  }

  @Test
  void testCreateRRule_Success() {
    // Given
    RequestRRuleDto createDto = ResourceLoader.loadResource("rrule-create-request.json", RequestRRuleDto.class);
    Caretaker caretaker = ResourceLoader.loadResource("caretaker-entity.json", Caretaker.class);
    UUID caretakerId = caretaker.getCaretakerId();

    CaretakerRRule savedRRule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);

    when(entityManager.getReference(Caretaker.class, caretakerId)).thenReturn(caretaker);
    when(rruleRepository.save(any(CaretakerRRule.class))).thenReturn(savedRRule);

    // When
    RRuleDto result = rruleService.createRRule(createDto, caretakerId);

    // Then
    assertNotNull(result);
    assertEquals(savedRRule.getRruleId(), result.rruleId());
    assertEquals(savedRRule.getRrule(), result.rrule());
    assertEquals(savedRRule.getDescription(), result.description());
    verify(entityManager).getReference(Caretaker.class, caretakerId);
    verify(rruleRepository).save(any(CaretakerRRule.class));
  }

  @Test
  void testUpdateRRule_Success() {
    // Given
    CaretakerRRule existingRRule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);
    UUID rruleId = existingRRule.getRruleId();
    UUID caretakerId = existingRRule.getCaretaker().getCaretakerId();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(existingRRule));
    when(rruleRepository.save(any(CaretakerRRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, updateDto, caretakerId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(updateDto.rrule(), result.get().rrule());
    assertEquals(updateDto.description(), result.get().description());
    assertEquals(updateDto.dtstart(), result.get().dtstart());
    assertEquals(updateDto.dtend(), result.get().dtend());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository).save(any(CaretakerRRule.class));
  }

  @Test
  void testUpdateRRule_NotFound() {
    // Given
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);
    UUID rruleId = UUID.randomUUID();
    UUID caretakerId = UUID.randomUUID();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.empty());

    // When
    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, updateDto, caretakerId);

    // Then
    assertTrue(result.isEmpty());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository, never()).save(any());
  }

  @Test
  void testUpdateRRule_WrongCaretaker() {
    // Given
    CaretakerRRule existingRRule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);
    RequestRRuleDto updateDto = ResourceLoader.loadResource("rrule-update-request.json", RequestRRuleDto.class);
    UUID rruleId = existingRRule.getRruleId();
    UUID wrongCaretakerId = UUID.randomUUID();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(existingRRule));

    // When
    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, updateDto, wrongCaretakerId);

    // Then
    assertTrue(result.isEmpty());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository, never()).save(any());
  }

  @Test
  void testDeleteRRule_Success() {
    // Given
    CaretakerRRule existingRRule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);
    UUID rruleId = existingRRule.getRruleId();
    UUID caretakerId = existingRRule.getCaretaker().getCaretakerId();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(existingRRule));

    // When
    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, caretakerId);

    // Then
    assertTrue(result.isPresent());
    assertEquals(existingRRule.getRruleId(), result.get().rruleId());
    assertEquals(existingRRule.getRrule(), result.get().rrule());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository).deleteById(rruleId);
  }

  @Test
  void testDeleteRRule_NotFound() {
    // Given
    UUID rruleId = UUID.randomUUID();
    UUID caretakerId = UUID.randomUUID();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.empty());

    // When
    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, caretakerId);

    // Then
    assertTrue(result.isEmpty());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository, never()).deleteById(any());
  }

  @Test
  void testDeleteRRule_WrongCaretaker() {
    // Given
    CaretakerRRule existingRRule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);
    UUID rruleId = existingRRule.getRruleId();
    UUID wrongCaretakerId = UUID.randomUUID();

    when(rruleRepository.findById(rruleId)).thenReturn(Optional.of(existingRRule));

    // When
    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, wrongCaretakerId);

    // Then
    assertTrue(result.isEmpty());
    verify(rruleRepository).findById(rruleId);
    verify(rruleRepository, never()).deleteById(any());
  }

  @Test
  void testConvertToDto_Success() {
    // Given
    CaretakerRRule rrule = ResourceLoader.loadResource("rrule-entity.json", CaretakerRRule.class);

    // When
    RRuleDto result = CaretakerRRuleService.convertToDto(rrule);

    // Then
    assertNotNull(result);
    assertEquals(rrule.getRruleId(), result.rruleId());
    assertEquals(rrule.getRrule(), result.rrule());
    assertEquals(rrule.getDtstart(), result.dtstart());
    assertEquals(rrule.getDtend(), result.dtend());
    assertEquals(rrule.getDescription(), result.description());
  }
}

