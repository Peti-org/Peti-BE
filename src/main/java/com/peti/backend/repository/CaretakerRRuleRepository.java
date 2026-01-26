package com.peti.backend.repository;

import com.peti.backend.model.domain.CaretakerRRule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CaretakerRRuleRepository extends JpaRepository<CaretakerRRule, UUID> {

  List<CaretakerRRule> findAllByCaretaker_CaretakerId(UUID caretakerId);

  @Query("SELECT r FROM CaretakerRRule r WHERE r.dtstart <= :now AND (r.dtend IS NULL OR r.dtend >= :now)")
  List<CaretakerRRule> findAllActive(@Param("now") LocalDateTime now);
}

