package com.peti.backend.controller.order;

import com.peti.backend.dto.event.EventDto;
import com.peti.backend.dto.event.RequestEventDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.CurrentUser;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.event.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
@Tag(name = "Event", description = "Operations for managing events booked from a CaretakerRRule")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

  private final EventService eventService;

  @HasCaretakerRole
  @GetMapping("/caretaker/my")
  @Operation(summary = "List events for the current caretaker (excludes deleted)")
  public ResponseEntity<List<EventDto>> getCaretakerEvents(@CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(eventService.getEventsByCaretakerId(caretakerId));
  }

  @HasUserRole
  @GetMapping("/user/my")
  @Operation(summary = "List events for the current user (excludes deleted)")
  public ResponseEntity<List<EventDto>> getUserEvents(@CurrentUser UserProjection user) {
    return ResponseEntity.ok(eventService.getEventsByUserId(user.getUserId()));
  }

  @HasUserRole
  @PostMapping
  @Operation(summary = "Create event from a CaretakerRRule",
      description = "Creates an event in CREATED status. Triggers an Elastic slot rebuild for the caretaker.")
  @ApiResponse(responseCode = "200", description = "Event created")
  @ApiResponse(responseCode = "400", description = "Invalid time window or pets")
  @ApiResponse(responseCode = "404", description = "RRule not found")
  public ResponseEntity<EventDto> createEvent(
      @Valid @RequestBody RequestEventDto requestEventDto,
      @CurrentUser UserProjection user) {
    return ResponseEntity.ok(eventService.createEvent(requestEventDto, user.getUserId()));
  }

  @HasUserRole
  @DeleteMapping("/{eventId}")
  @Operation(summary = "Soft-delete an event", description = "Triggers an Elastic slot rebuild.")
  public ResponseEntity<Void> deleteEvent(
      @PathVariable UUID eventId,
      @CurrentUser UserProjection user) {
    if (eventService.deleteEvent(eventId, user.getUserId())) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.notFound().build();
  }
}
