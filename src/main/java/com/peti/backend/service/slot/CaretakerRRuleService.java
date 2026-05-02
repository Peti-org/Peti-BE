package com.peti.backend.service.slot;

import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import com.peti.backend.service.event.CaretakerSlotsRebuildTrigger;
import jakarta.persistence.EntityManager;
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
public class CaretakerRRuleService {

  private final CaretakerRRuleRepository rruleRepository;
  private final EntityManager entityManager;
  private final CaretakerSlotsRebuildTrigger slotsRebuildTrigger;

  public List<RRuleDto> getAllRRulesForCaretaker(UUID caretakerId) {
    List<CaretakerRRule> rrules = rruleRepository.findAllByCaretaker_CaretakerId(caretakerId);
    return rrules.stream()
        .map(RRuleDto::convert)
        .collect(Collectors.toList());
  }

  @Transactional
  public RRuleDto createRRule(RequestRRuleDto createDto, UUID caretakerId) {
    CaretakerRRule saved = rruleRepository.save(buildRRule(createDto, caretakerId));
    // Trigger a rebuild of slots for caretaker
    slotsRebuildTrigger.rebuild(saved.getCaretaker());
    return RRuleDto.convert(saved);
  }

  @Transactional
  public Optional<RRuleDto> updateRRule(UUID rruleId, RequestRRuleDto updateDto, UUID caretakerId) {
    return rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId)
        .map(existing -> {
          applyFields(existing, updateDto);
          CaretakerRRule saved = rruleRepository.save(existing);
          // Trigger a rebuild of slots for caretaker
          slotsRebuildTrigger.rebuild(saved.getCaretaker());
          return RRuleDto.convert(saved);
        });
  }

  @Transactional
  public Optional<RRuleDto> deleteRRule(UUID rruleId, UUID caretakerId) {
    return rruleRepository.findByRruleIdAndCaretaker_CaretakerId(rruleId, caretakerId)
        .map(rrule -> {
          RRuleDto dto = RRuleDto.convert(rrule);
          Caretaker caretaker = rrule.getCaretaker();
          rruleRepository.deleteById(rruleId);
          // Trigger a rebuild of slots for caretaker
          slotsRebuildTrigger.rebuild(caretaker);
          return dto;
        });
  }

  private CaretakerRRule buildRRule(RequestRRuleDto request, UUID caretakerId) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setCaretaker(entityManager.getReference(Caretaker.class, caretakerId));
    applyFields(rrule, request);
    return rrule;
  }

  private void applyFields(CaretakerRRule rrule, RequestRRuleDto dto) {
    rrule.setRrule(dto.rrule());
    rrule.setDtstart(dto.dtstart());
    rrule.setDtend(dto.dtend());
    rrule.setDescription(dto.description());
    rrule.setSlotType(dto.slotType().name());
    rrule.setCapacity(dto.capacity());
    rrule.setIntervalMinutes(dto.intervalMinutes());
    rrule.setGeneratedTo(null); // Initially not generated
    rrule.setIsEnabled(dto.isEnabled());
    rrule.setIsSchedule(dto.isSchedule());
    rrule.setIsBusy(dto.isBusy());
    rrule.setPriority(dto.priority());
  }
}

