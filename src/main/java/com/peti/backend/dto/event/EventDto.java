package com.peti.backend.dto.event;

import com.peti.backend.dto.PriceDto;
import com.peti.backend.model.domain.Event;
import com.peti.backend.model.domain.Pet;
import com.peti.backend.model.internal.EventStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import org.jspecify.annotations.NonNull;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Schema(description = "Event details")
public record EventDto(
    UUID eventId,
    UUID caretakerId,
    UUID userId,
    UUID rruleId,
    LocalDateTime datetimeFrom,
    LocalDateTime datetimeTo,
    List<UUID> petIds,
    PriceDto price,
    EventStatus status) {

  public static EventDto from(Event event) {
    return new EventDto(
        event.getEventId(),
        event.getCaretaker().getCaretakerId(),
        event.getUser().getUserId(),
        event.getRrule() != null ? event.getRrule().getRruleId() : null,
        event.getDatetimeFrom(),
        event.getDatetimeTo(),
        getPetIds(event).stream().sorted().toList(),
        event.getPrice(),
        event.getStatus()
    );
  }

  private static @NonNull Set<UUID> getPetIds(Event event) {
    return event.getPets() == null
        ? Set.of()
        : event.getPets().stream()
            .map(Pet::getPetId)
            .collect(Collectors.toSet());
  }
}

