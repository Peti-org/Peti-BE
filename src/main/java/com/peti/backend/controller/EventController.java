package com.peti.backend.controller;

import com.peti.backend.dto.event.EventDto;
import com.peti.backend.dto.event.RequestEventDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.CaretakerService;
import com.peti.backend.service.EventService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event", description = "Operation that is needed for managing events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

  private final EventService eventService;
  private final CaretakerService caretakerService;

  @HasCaretakerRole
  @GetMapping("/caretaker/my")
  public ResponseEntity<List<EventDto>> getCaretakerEvents(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    UUID caretakerId = getCaretakerId(userProjection);
    return ResponseEntity.ok(eventService.getEventsByCaretakerId(caretakerId));
  }

  @HasUserRole
  @GetMapping("/user/my")
  public ResponseEntity<List<EventDto>> getUserEvents(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    return ResponseEntity.ok(eventService.getEventsByUserId(userProjection.getUserId()));
  }

  @HasUserRole
  @PostMapping
  public ResponseEntity<EventDto> createEvent(@Valid @RequestBody RequestEventDto requestEventDto,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    return ResponseEntity.ok(eventService.createEvent(requestEventDto, userProjection.getUserId()));
  }

  @HasUserRole
  @DeleteMapping("/{eventId}")
  public ResponseEntity<SlotDto> deleteEvent(@PathVariable UUID eventId,
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    if (eventService.deleteEvent(eventId, userProjection.getUserId())) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }

  private UUID getCaretakerId(UserProjection userProjection) {
    try {
      return caretakerService.getCaretakerIdByUserId(userProjection.getUserId())
          .orElseThrow(
              () -> new IllegalArgumentException("Caretaker not found for user: " + userProjection.getUserId()));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }

  @ModelAttribute("userProjection")
  public UserProjection getUserProjection(Authentication authentication) {
    try {
      return (UserProjection) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}
