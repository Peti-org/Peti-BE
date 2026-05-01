package com.peti.backend.repository;

import com.peti.backend.model.domain.Event;
import com.peti.backend.model.internal.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

  List<Event> findAllByCaretaker_CaretakerIdAndStatusNot(UUID caretakerId, EventStatus excluded);

  List<Event> findAllByUser_UserIdAndStatusNot(UUID userId, EventStatus excluded);

  Optional<Event> findByEventIdAndStatusNot(UUID eventId, EventStatus excluded);
  /**
   * Find non-deleted events for the caretaker that overlap the given day window
   * [{@code dayStart}, {@code dayEnd}).
   */
  @Query("""
      SELECT e FROM Event e
      WHERE e.caretaker.caretakerId = :caretakerId
        AND e.status <> com.peti.backend.model.internal.EventStatus.DELETED
        AND e.datetimeFrom < :dayEnd
        AND e.datetimeTo > :dayStart
      """)
  List<Event> findActiveOverlapping(
      @Param("caretakerId") UUID caretakerId,
      @Param("dayStart") LocalDateTime dayStart,
      @Param("dayEnd") LocalDateTime dayEnd);
}

