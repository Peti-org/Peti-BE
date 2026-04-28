package com.peti.backend.service.slot;

import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.model.domain.Caretaker;
import com.peti.backend.model.domain.CaretakerRRule;
import com.peti.backend.model.internal.ServiceType;
import com.peti.backend.repository.CaretakerRRuleRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
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
public class CaretakerRRuleService {

  private final CaretakerRRuleRepository rruleRepository;
  private final EntityManager entityManager;
  private final SlotGenerationScheduler slotGenerationScheduler;

  public static RRuleDto convertToDto(CaretakerRRule rrule) {
    return new RRuleDto(
        rrule.getRruleId(),
        rrule.getRrule(),
        rrule.getDtstart(),
        rrule.getDtend(),
        rrule.getDescription(),
        ServiceType.fromName(rrule.getSlotType()),
        rrule.getCapacity(),
        rrule.getIntervalMinutes(),
        rrule.getIsEnabled(),
        rrule.getIsSchedule(),
        rrule.getIsBusy(),
        rrule.getPriority());
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
    rrule.setSlotType(createDto.slotType().name());
    rrule.setCapacity(createDto.capacity());
    rrule.setIntervalMinutes(createDto.intervalMinutes());
    rrule.setGeneratedTo(null); // Initially not generated
    rrule.setCreatedAt(LocalDateTime.now());
    rrule.setIsEnabled(createDto.isEnabled());
    rrule.setIsSchedule(createDto.isSchedule());
    rrule.setIsBusy(createDto.isBusy());
    rrule.setPriority(createDto.priority());

    CaretakerRRule saved = rruleRepository.save(rrule);

    // Trigger immediate slot generation for the next 14 days
    try {
      LocalDate today = LocalDate.now();
      LocalDate endDate = today.plusDays(14);
      slotGenerationScheduler.generateSlotsForSingleRRule(saved.getRruleId(), today, endDate);
      log.info("Generated initial slots for new RRule {}", saved.getRruleId());
    } catch (Exception e) {
      log.error("Failed to generate initial slots for RRule {}: {}", saved.getRruleId(), e.getMessage(), e);
    }

    return convertToDto(saved);
  }

  @Transactional
  public Optional<RRuleDto> updateRRule(UUID rruleId, RequestRRuleDto updateDto, UUID caretakerId) {
    return rruleRepository.findById(rruleId)
        .filter(rrule -> rrule.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(existing -> {
          // Delete future unoccupied slots before updating
          try {
            slotGenerationScheduler.deleteSlotsForRRule(rruleId, LocalDate.now());
            log.info("Deleted future unoccupied slots for RRule {} before update", rruleId);
          } catch (Exception e) {
            log.error("Failed to delete slots for RRule {}: {}", rruleId, e.getMessage(), e);
          }

          existing.setRrule(updateDto.rrule());
          existing.setDtstart(updateDto.dtstart());
          existing.setDtend(updateDto.dtend());
          existing.setDescription(updateDto.description());
          existing.setSlotType(updateDto.slotType().name());
          existing.setCapacity(updateDto.capacity());
          existing.setIntervalMinutes(updateDto.intervalMinutes());
          existing.setGeneratedTo(null); // Reset generation tracking
          existing.setIsEnabled(updateDto.isEnabled());
          existing.setIsSchedule(updateDto.isSchedule());
          existing.setIsBusy(updateDto.isBusy());
          existing.setPriority(updateDto.priority());

          CaretakerRRule saved = rruleRepository.save(existing);

          // Regenerate slots with new settings
          try {
            LocalDate today = LocalDate.now();
            LocalDate endDate = today.plusDays(14);
            slotGenerationScheduler.generateSlotsForSingleRRule(saved.getRruleId(), today, endDate);
            log.info("Regenerated slots for updated RRule {}", saved.getRruleId());
          } catch (Exception e) {
            log.error("Failed to regenerate slots for RRule {}: {}", saved.getRruleId(), e.getMessage(), e);
          }

          return convertToDto(saved);
        });
  }

  @Transactional
  public Optional<RRuleDto> deleteRRule(UUID rruleId, UUID caretakerId) {
    return rruleRepository.findById(rruleId)
        .filter(rrule -> rrule.getCaretaker().getCaretakerId().equals(caretakerId))
        .map(rrule -> {
          RRuleDto dto = convertToDto(rrule);

          // Delete all future unoccupied slots generated by this RRule
          try {
            slotGenerationScheduler.deleteSlotsForRRule(rruleId, LocalDate.now());
            log.info("Deleted future unoccupied slots for deleted RRule {}", rruleId);
          } catch (Exception e) {
            log.error("Failed to delete slots for RRule {}: {}", rruleId, e.getMessage(), e);
          }

          rruleRepository.deleteById(rruleId);
          return dto;
        });
  }
}

