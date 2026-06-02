package com.peti.backend.repository;

import com.peti.backend.model.domain.CaretakerRRule;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CaretakerRRuleRepository extends JpaRepository<CaretakerRRule, UUID> {

  List<CaretakerRRule> findAllByCaretaker_CaretakerId(UUID caretakerId);

  List<CaretakerRRule> findAllByCaretaker_CaretakerIdAndSlotTypeAndIsEnabledTrue(
      UUID caretakerId, String slotType);

  Optional<CaretakerRRule> findByRruleIdAndCaretaker_CaretakerId(UUID id, UUID caretakerId);

  @Query("SELECT r FROM CaretakerRRule r WHERE r.isEnabled = true")
  List<CaretakerRRule> findAllActive();
}

