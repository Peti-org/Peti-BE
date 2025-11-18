package com.peti.backend.repository;

import com.peti.backend.model.domain.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

  List<Event> findAllByCaretaker_CaretakerId(UUID caretakerId);

  List<Event> findAllByUser_UserId(UUID userUserId);

  boolean existsByEventIdAndUser_UserId(UUID eventId, UUID userUserId);
}
