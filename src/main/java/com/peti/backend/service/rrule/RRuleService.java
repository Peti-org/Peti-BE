package com.peti.backend.service.rrule;

import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.service.slot.SlotsRebuildTrigger;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RRuleService {

  private final CaretakerRRuleRepository rruleRepository;
  private final EntityManager entityManager;
  private final SlotsRebuildTrigger slotsRebuildTrigger;

  public List<RRuleDto> getAllRRulesForCaretaker(UUID caretakerId) {
    List<CaretakerRRule> rrules =
        rruleRepository.findAllByCaretaker_CaretakerIdAndIsEnabledTrue(caretakerId);
    return rrules.stream()
        .map(RRuleDto::convert)
        .collect(Collectors.toList());
  }

  public List<RRuleDto> getScheduleCaretaker(UUID caretakerId) {
    List<CaretakerRRule> rrules =
        rruleRepository.findAllByCaretaker_CaretakerIdAndIsScheduleTrue(caretakerId);
    return rrules.stream()
        .map(RRuleDto::convert)
        .collect(Collectors.toList());
  }

  @Transactional
  public RRuleDto createRRule(RequestRRuleDto createDto, UUID caretakerId) {
    CaretakerRRule saved = rruleRepository.save(buildRRule(createDto, caretakerId));
    // Trigger a rebuild of slots for caretaker
    slotsRebuildTrigger.rebuildAsync(caretakerId);
    return RRuleDto.convert(saved);
  }

  @Transactional
  public Optional<RRuleDto> updateRRule(UUID rruleId, RequestRRuleDto updateDto, UUID caretakerId) {
    return rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId)
        .map(existing -> {
          applyFields(existing, updateDto);
          CaretakerRRule saved = rruleRepository.save(existing);
          // Trigger a rebuild of slots for caretaker
          slotsRebuildTrigger.rebuildAsync(caretakerId);
          return RRuleDto.convert(saved);
        });
  }

  @Transactional
  public Optional<RRuleDto> setEnabled(UUID rruleId, UUID caretakerId, boolean enabled) {
    return rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId)
        .map(existing -> {
          existing.setIsEnabled(enabled);
          existing.setUpdatedAt(LocalDateTime.now());
          CaretakerRRule saved = rruleRepository.save(existing);
          slotsRebuildTrigger.rebuildAsync(caretakerId);
          return RRuleDto.convert(saved);
        });
  }

  @Transactional
  public Optional<RRuleDto> deleteRRule(UUID rruleId, UUID caretakerId) {
    return rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId)
        .map(rrule -> {
          RRuleDto dto = RRuleDto.convert(rrule);
          rruleRepository.delete(rrule);
          // Trigger a rebuild of slots for caretaker
          slotsRebuildTrigger.rebuildAsync(caretakerId);
          return dto;
        });
  }

  private CaretakerRRule buildRRule(RequestRRuleDto request, UUID caretakerId) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setCaretaker(entityManager.getReference(Caretaker.class, caretakerId));
    rrule.setCreatedAt(LocalDateTime.now());
    rrule.setIsEnabled(true);
    applyFields(rrule, request);
    return rrule;
  }

  private void applyFields(CaretakerRRule rrule, RequestRRuleDto dto) {
    rrule.setRrule(dto.rrule());
    rrule.setSlotStartTime(dto.slotStartTime());
    rrule.setSlotDuration(dto.slotDuration());
    rrule.setDescription(dto.description());
    rrule.setSlotType(dto.slotType().name());
    rrule.setPetCapacity(dto.petCapacity());
    rrule.setPeopleCapacity(dto.peopleCapacity());
    rrule.setIsSchedule(dto.isSchedule());
    rrule.setIsBusy(dto.isBusy());
    rrule.setPriority(dto.priority());
    rrule.setUpdatedAt(LocalDateTime.now());
  }
}

