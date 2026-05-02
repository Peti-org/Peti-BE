package com.peti.backend.repository;

import com.peti.backend.model.domain.CaretakerRRule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaretakerRRuleRepository extends JpaRepository<CaretakerRRule, UUID> {

  List<CaretakerRRule> findAllByCaretaker_CaretakerId(UUID caretakerId);

  Optional<CaretakerRRule> findByRruleIdAndCaretaker_CaretakerId(UUID id, UUID caretakerId);

  @Query("SELECT r FROM CaretakerRRule r WHERE r.dtstart <= :now AND (r.dtend IS NULL OR r.dtend >= :now)")
  List<CaretakerRRule> findAllActive(@Param("now") LocalDateTime now);

  @Query("SELECT r FROM CaretakerRRule r WHERE " +
      "(r.generatedTo IS NULL OR r.generatedTo < :targetDate) " +
      "AND r.dtstart <= :now " +
      "AND (r.dtend IS NULL OR r.dtend >= :now)")
  List<CaretakerRRule> findAllNeedingGeneration(
      @Param("targetDate") LocalDate targetDate,
      @Param("now") LocalDateTime now);
}

