package com.peti.backend.repository;

import com.peti.backend.model.domain.CaretakerRRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaretakerRRuleRepository extends JpaRepository<CaretakerRRule, UUID> {

  List<CaretakerRRule> findAllByCaretaker_CaretakerIdAndIsEnabledTrue(UUID caretakerId);

  List<CaretakerRRule> findAllByCaretaker_CaretakerIdAndSlotType(
      UUID caretakerId, String slotType);

  List<CaretakerRRule> findAllByCaretaker_CaretakerIdAndIsScheduleTrue(
      UUID caretakerId);

  Optional<CaretakerRRule> findByRruleIdAndCaretaker_CaretakerId(UUID id, UUID caretakerId);
}
