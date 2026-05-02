package com.peti.backend.controller.user;

import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.dto.caretacker.SimpleCaretakerDto;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.CurrentUser;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.slot.CaretakerRRuleService;
import com.peti.backend.service.user.CaretakerService;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caretakers")
@RequiredArgsConstructor
@Tag(name = "Caretaker", description = "Operation that is needed for managing caretaker")
@SecurityRequirement(name = "bearerAuth")
public class CaretakerController {

  private final CaretakerService caretakerService;
  private final CaretakerRRuleService rruleService;

  @HasAdminRole
  @GetMapping
  public ResponseEntity<List<SimpleCaretakerDto>> getAllCaretakers() {
    return ResponseEntity.ok(caretakerService.getAllCaretakers());
  }

  @HasUserRole
  @GetMapping("/me")
  public ResponseEntity<CaretakerDto> getMyCaretakerDetails(@CurrentCaretakerId UUID caretakerId) {
    return caretakerService.getCaretakerById(caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @PostMapping
  public ResponseEntity<CaretakerDto> createCaretaker(@CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(caretakerService.createCaretaker(userProjection));
  }

  @HasUserRole
  @PutMapping
  public ResponseEntity<CaretakerDto> updateCaretaker(
      @CurrentUser UserProjection userProjection,
      @RequestBody(
          description = "Caretaker preferences to update",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CaretakerPreferences.class)
          )
      )
      @org.springframework.web.bind.annotation.RequestBody @Valid CaretakerPreferences caretakerPreferences) {
    return ResponseEntity.ok(caretakerService.updateCaretaker(userProjection, caretakerPreferences));
  }

  @GetMapping("/{caretakerId}/rrules")
  public ResponseEntity<List<RRuleDto>> getCaretakerRRules(@PathVariable UUID caretakerId) {
    return ResponseEntity.ok(rruleService.getAllRRulesForCaretaker(caretakerId));
  }
}
