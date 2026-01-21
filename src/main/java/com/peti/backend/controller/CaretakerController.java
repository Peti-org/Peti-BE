package com.peti.backend.controller;

import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.exception.NotFoundException;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.CaretakerService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
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
