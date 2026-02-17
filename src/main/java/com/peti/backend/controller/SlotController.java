package com.peti.backend.controller;

import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.dto.slot.RequestSlotDto;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.service.CaretakerRRuleService;
import com.peti.backend.service.CaretakerService;
import com.peti.backend.service.SlotService;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
@Tag(name = "Catalog", description = "Operation that is needed for catalog page")
@SecurityRequirement(name = "bearerAuth")
public class SlotController {

  private final SlotService slotService;
  private final CaretakerService caretakerService;
  private final CaretakerRRuleService rruleService;

  @HasCaretakerRole
  @GetMapping("/my")
  public ResponseEntity<List<SlotDto>> getCaretakerSlots(@Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return ResponseEntity.ok(slotService.getCaretakerSlots(caretakerId));
  }

  @HasCaretakerRole
  @PostMapping
  public ResponseEntity<List<SlotDto>> createSlot(@Valid @RequestBody RequestSlotDto requestSlotDto,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return ResponseEntity.ok(slotService.createSlot(requestSlotDto, caretakerId));
  }

  @HasCaretakerRole
  @PutMapping("/{slotId}")
  public ResponseEntity<SlotDto> updateSlot(@PathVariable UUID slotId,
      @Valid @RequestBody RequestSlotDto requestSlotDto,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return slotService.updateSlot(slotId, requestSlotDto, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasCaretakerRole
  @DeleteMapping("/{slotId}")
  public ResponseEntity<SlotDto> deleteSlot(@PathVariable UUID slotId,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return slotService.deleteSlot(slotId, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // RRule endpoints
  @HasCaretakerRole
  @GetMapping("/rrules")
  public ResponseEntity<List<RRuleDto>> getAllRRules(
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return ResponseEntity.ok(rruleService.getAllRRulesForCaretaker(caretakerId));
  }

  @HasCaretakerRole
  @PostMapping("/rrules")
  public ResponseEntity<RRuleDto> createRRule(@Valid @RequestBody RequestRRuleDto createRRuleDto,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return ResponseEntity.ok(rruleService.createRRule(createRRuleDto, caretakerId));
  }

  @HasCaretakerRole
  @PutMapping("/rrules/{rruleId}")
  public ResponseEntity<RRuleDto> updateRRule(@PathVariable UUID rruleId,
      @Valid @RequestBody RequestRRuleDto requestRRuleDto,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return rruleService.updateRRule(rruleId, requestRRuleDto, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasCaretakerRole
  @DeleteMapping("/rrules/{rruleId}")
  public ResponseEntity<RRuleDto> deleteRRule(@PathVariable UUID rruleId,
      @Parameter(hidden = true) @ModelAttribute("caretakerId") UUID caretakerId) {
    return rruleService.deleteRRule(rruleId, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @ModelAttribute("caretakerId")
  public UUID getCaretakerId(Authentication authentication) {
    try {
      UserProjection userProjection = (UserProjection) authentication.getPrincipal();
      return caretakerService.getCaretakerIdByUserId(userProjection.getUserId())
          .orElseThrow(
              () -> new IllegalArgumentException("Caretaker not found for user: " + userProjection.getUserId()));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}
