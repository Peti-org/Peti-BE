package com.peti.backend.controller;

import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.service.SlotGenerationScheduler;
import com.peti.backend.service.SlotGenerationScheduler.SlotGenerationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for managing slot generation operations.
 */
@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@Tag(name = "Admin - Slot Management", description = "Administrative operations for slot generation")
@SecurityRequirement(name = "bearerAuth")
public class AdminSlotController {

  private final SlotGenerationScheduler slotGenerationScheduler;

  /**
   * Manually trigger slot generation for all active RRules.
   * This endpoint allows administrators to generate slots on-demand
   * without waiting for the scheduled job.
   *
   * @return Statistics about the generation process
   */
  @HasAdminRole
  @PostMapping("/generate")
  @Operation(summary = "Manually trigger slot generation",
      description = "Generates slots for all active RRules for the next 14 days. " +
          "This can be used to manually trigger slot generation without waiting for the scheduled job.")
  public ResponseEntity<SlotGenerationResult> generateSlots() {
    SlotGenerationResult result = slotGenerationScheduler.generateSlots();
    return ResponseEntity.ok(result);
  }
}

