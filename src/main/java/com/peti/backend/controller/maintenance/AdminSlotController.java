package com.peti.backend.controller.maintenance;

import com.peti.backend.dto.slot.SlotGenerationRequest;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.service.slot.SlotGenerationScheduler;
import com.peti.backend.service.slot.SlotGenerationScheduler.SlotGenerationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin controller for managing slot generation operations.
 */
@RestController
@RequestMapping("/api/admin/slots")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin operations")
@SecurityRequirement(name = "bearerAuth")
public class AdminSlotController {

  private final SlotGenerationScheduler slotGenerationScheduler;

  /**
   * Manually trigger slot generation.
   *
   * @param request generation parameters (mode, optional date range, optional caretakerId)
   * @return statistics about the generation process
   */
  @HasAdminRole
  @PostMapping("/generate")
  @Operation(summary = "Manually trigger slot generation",
      description = "Generates slots with specified mode (DEFAULT=incremental, FORCE=full). "
          + "Optionally specify caretakerId, dateFrom, dateTo.")
  public ResponseEntity<SlotGenerationResult> generateSlots(
      @RequestBody SlotGenerationRequest request) {
    SlotGenerationResult result = slotGenerationScheduler.generateSlots(request);
    return ResponseEntity.ok(result);
  }
}

