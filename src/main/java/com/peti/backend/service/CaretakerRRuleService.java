package com.peti.backend.service;

import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.repository.CaretakerRRuleRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaretakerRRuleService {

  private final CaretakerRRuleRepository rruleRepository;
  private final EntityManager entityManager;

  public static RRuleDto convertToDto(CaretakerRRule rrule) {
    return new RRuleDto(
        rrule.getRruleId(),
        rrule.getRrule(),
        rrule.getDtstart(),
        rrule.getDtend(),
        rrule.getDescription(),
        rrule.getSlotType());
  }

  public List<RRuleDto> getAllRRulesForCaretaker(UUID caretakerId) {
    List<CaretakerRRule> rrules = rruleRepository.findAllByCaretaker_CaretakerId(caretakerId);
    return rrules.stream()
        .map(CaretakerRRuleService::convertToDto)
        .collect(Collectors.toList());
  }

  @Transactional
  public RRuleDto createRRule(RequestRRuleDto createDto, UUID caretakerId) {
    CaretakerRRule rrule = new CaretakerRRule();
    rrule.setCaretaker(entityManager.getReference(Caretaker.class, caretakerId));
    rrule.setRrule(createDto.rrule());
    rrule.setDtstart(createDto.dtstart());
    rrule.setDtend(createDto.dtend());
    rrule.setDescription(createDto.description());
    rrule.setSlotType(createDto.slotType());
    rrule.setCreatedAt(LocalDateTime.now());

    CaretakerRRule saved = rruleRepository.save(rrule);
    return convertToDto(saved);
  }

  @Transactional
  public Optional<RRuleDto> updateRRule(UUID rruleId, RequestRRuleDto updateDto, UUID caretakerId) {
    return rruleRepository.findById(rruleId)
        .filter(rrule -> rrule.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(existing -> {
          existing.setRrule(updateDto.rrule());
          existing.setDtstart(updateDto.dtstart());
          existing.setDtend(updateDto.dtend());
          existing.setDescription(updateDto.description());
          existing.setSlotType(updateDto.slotType());
          CaretakerRRule saved = rruleRepository.save(existing);
          return convertToDto(saved);
        });
  }

  @Transactional
  public Optional<RRuleDto> deleteRRule(UUID rruleId, UUID caretakerId) {
    return rruleRepository.findById(rruleId)
        .filter(rrule -> rrule.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(rrule -> {
          RRuleDto dto = convertToDto(rrule);
          rruleRepository.deleteById(rruleId);
          return dto;
        });
  }
}

