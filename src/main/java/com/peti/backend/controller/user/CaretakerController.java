package com.peti.backend.controller.user;

import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.caretacker.CaretakerPreferences;
import com.peti.backend.model.exception.NotFoundException;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.user.CaretakerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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

  @HasAdminRole
  @GetMapping
  public ResponseEntity<List<CaretakerDto>> getAllCaretakers() {
    return ResponseEntity.ok(caretakerService.getAllCaretakers());
  }

  @HasUserRole
  @GetMapping("/me")
  public ResponseEntity<CaretakerDto> getCaretakerById(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    try {
      UUID caretakerId = caretakerService.getCaretakerIdByUserId(userProjection.getUserId())
          .orElseThrow(
              () -> new NotFoundException("Caretaker not found for user: " + userProjection.getUserId()));
      return caretakerService.getCaretakerById(caretakerId)
          .map(ResponseEntity::ok)
          .orElse(ResponseEntity.notFound().build());
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }

  @HasUserRole
  @PostMapping
  public ResponseEntity<CaretakerDto> createCaretaker(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection) {
    return ResponseEntity.ok(caretakerService.createCaretaker(userProjection));
  }

  @HasUserRole
  @PutMapping
  public ResponseEntity<CaretakerDto> updateCaretaker(
      @Parameter(hidden = true) @ModelAttribute("userProjection") UserProjection userProjection,
      @RequestBody(
          description = "Caretaker preferences to update",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = CaretakerPreferences.class)
          )
      )
      @org.springframework.web.bind.annotation.RequestBody @Valid CaretakerPreferences caretakerPreferences
  ) {
    return ResponseEntity.ok(caretakerService.updateCaretaker(userProjection, caretakerPreferences));
  }

  @ModelAttribute("userProjection")
  public UserProjection getUserProjection(Authentication authentication) {
    try {
      UserProjection projection = (UserProjection) authentication.getPrincipal();
      return projection;
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}
