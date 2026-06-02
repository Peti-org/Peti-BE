package com.peti.backend.service.rrule;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.service.slot.SlotsRebuildTrigger;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaretakerRRuleServiceTest {

  @Mock
  private CaretakerRRuleRepository rruleRepository;

  @Mock
  private EntityManager entityManager;

  @Mock
  private SlotsRebuildTrigger slotsRebuildTrigger;

  @InjectMocks
  private CaretakerRRuleService rruleService;

  private UUID caretakerId;
  private UUID rruleId;
  private CaretakerRRule rrule;
  private RequestRRuleDto requestRRuleDto;
  private Caretaker caretaker;

  @BeforeEach
  void setUp() {
    caretakerId = UUID.fromString("223e4567-e89b-12d3-a456-426614174002");
    rruleId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

    requestRRuleDto = ResourceLoader.loadResource("rrule-create-request.json", RequestRRuleDto.class);

    caretaker = new Caretaker();
    caretaker.setCaretakerId(caretakerId);

    rrule = new CaretakerRRule();
    rrule.setRruleId(rruleId);
    rrule.setCaretaker(caretaker);
    rrule.setRrule(requestRRuleDto.rrule());
    rrule.setSlotStartTime(requestRRuleDto.dtstart() != null ? requestRRuleDto.dtstart().toLocalTime() : null);
    rrule.setSlotDuration(requestRRuleDto.dtstart() != null && requestRRuleDto.dtend() != null
        ? java.time.Duration.between(requestRRuleDto.dtstart(), requestRRuleDto.dtend()) : null);
    rrule.setDescription(requestRRuleDto.description());
    rrule.setSlotType(requestRRuleDto.slotType().name());
    rrule.setCapacity(requestRRuleDto.capacity());
    rrule.setIntervalMinutes(requestRRuleDto.intervalMinutes());
    rrule.setIsEnabled(requestRRuleDto.isEnabled());
    rrule.setIsSchedule(requestRRuleDto.isSchedule());
    rrule.setIsBusy(requestRRuleDto.isBusy());
    rrule.setPriority(requestRRuleDto.priority());
  }

  @Test
  void testCreateRRule_Success() {
    when(entityManager.getReference(Caretaker.class, caretakerId)).thenReturn(caretaker);
    when(rruleRepository.save(any(CaretakerRRule.class))).thenReturn(rrule);

    RRuleDto result = rruleService.createRRule(requestRRuleDto, caretakerId);

    assertNotNull(result);
    assertEquals(requestRRuleDto.rrule(), result.rrule());
    assertEquals(requestRRuleDto.capacity(), result.capacity());
    assertEquals(requestRRuleDto.intervalMinutes(), result.intervalMinutes());
    assertEquals(requestRRuleDto.isEnabled(), result.isEnabled());
    assertEquals(requestRRuleDto.isSchedule(), result.isSchedule());
    assertEquals(requestRRuleDto.isBusy(), result.isBusy());
    assertEquals(requestRRuleDto.priority(), result.priority());
    verify(rruleRepository).save(any(CaretakerRRule.class));
    verify(slotsRebuildTrigger).rebuildAsync(caretaker.getCaretakerId());
  }

  @Test
  void testGetAllRRulesForCaretaker() {
    when(rruleRepository.findAllByCaretaker_CaretakerId(caretakerId)).thenReturn(List.of(rrule));

    List<RRuleDto> result = rruleService.getAllRRulesForCaretaker(caretakerId);

    assertEquals(1, result.size());
    assertEquals(rruleId, result.get(0).rruleId());
  }

  @Test
  void testUpdateRRule_Success() {
    when(rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId))
        .thenReturn(Optional.of(rrule));
    when(rruleRepository.save(any(CaretakerRRule.class))).thenReturn(rrule);

    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, requestRRuleDto, caretakerId);

    assertTrue(result.isPresent());
    verify(rruleRepository).save(any(CaretakerRRule.class));
    verify(slotsRebuildTrigger).rebuildAsync(caretaker.getCaretakerId());
  }

  @Test
  void testUpdateRRule_NotFound() {
    when(rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId))
        .thenReturn(Optional.empty());

    Optional<RRuleDto> result = rruleService.updateRRule(rruleId, requestRRuleDto, caretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  void testDeleteRRule_Success() {
    when(rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId))
        .thenReturn(Optional.of(rrule));

    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, caretakerId);

    assertTrue(result.isPresent());
    verify(rruleRepository).deleteById(rruleId);
    verify(slotsRebuildTrigger).rebuildAsync(caretaker.getCaretakerId());
  }

  @Test
  void testDeleteRRule_NotFound() {
    when(rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId))
        .thenReturn(Optional.empty());

    Optional<RRuleDto> result = rruleService.deleteRRule(rruleId, caretakerId);

    assertFalse(result.isPresent());
  }

  @Test
  void testConvertToDto() {
    RRuleDto result = RRuleDto.convert(rrule);

    assertNotNull(result);
    assertEquals(rruleId, result.rruleId());
    assertEquals(requestRRuleDto.rrule(), result.rrule());
    assertEquals(requestRRuleDto.capacity(), result.capacity());
    assertEquals(requestRRuleDto.intervalMinutes(), result.intervalMinutes());
    assertEquals(requestRRuleDto.isEnabled(), result.isEnabled());
    assertEquals(requestRRuleDto.isSchedule(), result.isSchedule());
    assertEquals(requestRRuleDto.isBusy(), result.isBusy());
    assertEquals(requestRRuleDto.priority(), result.priority());
  }
}
