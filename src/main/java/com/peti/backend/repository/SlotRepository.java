package com.peti.backend.repository;

import com.peti.backend.model.domain.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID>, SlotFilteringRepository {

  List<Slot> findAllByCaretaker_CaretakerId(UUID caretakerId);

  List<Slot> findAllByCaretaker_CaretakerIdAndDateBetween(UUID caretakerCaretakerId, Date dateAfter, Date dateBefore);

  @Query("SELECT COUNT(s) > 0 FROM Slot s WHERE s.caretaker.caretakerId = :caretakerId " +
      "AND s.date = :date AND s.timeFrom = :timeFrom AND s.timeTo = :timeTo")
  boolean existsByCaretakerIdAndDateAndTime(
      @Param("caretakerId") UUID caretakerId,
      @Param("date") Date date,
      @Param("timeFrom") Time timeFrom,
      @Param("timeTo") Time timeTo);
}

