package com.peti.backend.repository;

import com.peti.backend.model.domain.Slot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.sql.Date;
import java.util.List;

import java.util.UUID;

@Repository
public interface SlotRepository extends JpaRepository<Slot, UUID>, SlotFilteringRepository {

  List<Slot> findAllByCaretaker_CaretakerId(UUID caretakerId);

  List<Slot> findAllByCaretaker_CaretakerIdAndDateBetween(UUID caretakerCaretakerId, Date dateAfter, Date dateBefore);
}

