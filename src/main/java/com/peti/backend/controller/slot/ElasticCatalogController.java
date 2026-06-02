package com.peti.backend.controller.slot;

import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchResponse;
import com.peti.backend.dto.rrule.RRuleDto;
import com.peti.backend.dto.rrule.RequestRRuleDto;
import com.peti.backend.security.annotation.CurrentCaretakerId;
import com.peti.backend.security.annotation.HasCaretakerRole;
import com.peti.backend.service.rrule.CaretakerRRuleService;
import com.peti.backend.service.slot.SlotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping()
@Tag(name = "Catalog V2 (Elasticsearch)", description = "Elasticsearch-based catalog with flexible slots")
public class ElasticCatalogController {

  private final SlotSearchService searchService;
  private final CaretakerRRuleService rruleService;

  @PostMapping("/api/v2/catalog/search")
  @Operation(summary = "Search available slots",
      description = "Search for available slots with filters. Returns aggregated results grouped by caretaker.")
  public ResponseEntity<ElasticSlotSearchResponse> searchSlots(@Valid @RequestBody ElasticSlotSearchRequest request) {
    return ResponseEntity.ok(searchService.searchSlots(request));
  }

  @GetMapping("/api/caretakers/{caretakerId}/rrules")
  public ResponseEntity<List<RRuleDto>> getRRules(@PathVariable UUID caretakerId) {
    return ResponseEntity.ok(rruleService.getAllRRulesForCaretaker(caretakerId));
  }

  @HasCaretakerRole
  @GetMapping("/api/caretakers/my/rrules")
  public ResponseEntity<List<RRuleDto>> getMyAllRRules(@CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(rruleService.getAllRRulesForCaretaker(caretakerId));
  }

  @HasCaretakerRole
  @GetMapping("/api/caretakers/me/rrules/schedule")
  public ResponseEntity<List<RRuleDto>> getMySchedule(@CurrentCaretakerId UUID caretakerId) {
    return ResponseEntity.ok(rruleService.getScheduleCaretaker(caretakerId));
  }

  @HasCaretakerRole
  @PostMapping("/api/caretakers/me/rrules")
  public ResponseEntity<RRuleDto> createRRule(@CurrentCaretakerId UUID caretakerId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Caretaker rrule to create",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = RequestRRuleDto.class)
          )
      )
      @RequestBody @Valid RequestRRuleDto requestRRuleDto) {

    return ResponseEntity.ok(rruleService.createRRule(requestRRuleDto, caretakerId));
  }


  @HasCaretakerRole
  @PutMapping("/api/caretakers/me/rrules/{rruleId}")
  public ResponseEntity<RRuleDto> updateRRule(@CurrentCaretakerId UUID caretakerId,
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Caretaker rrule to update",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = RequestRRuleDto.class)
          )
      )
      @RequestBody @Valid RequestRRuleDto requestRRuleDto,
      @PathVariable UUID rruleId) {

    return rruleService.updateRRule(rruleId, requestRRuleDto, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasCaretakerRole
  @PatchMapping("/api/caretakers/me/rrules/{rruleId}/enabled")
  @Operation(summary = "Enable or disable a caretaker rrule",
      description = "Toggles the isEnabled flag of the rrule and triggers slot rebuild.")
  public ResponseEntity<RRuleDto> setRRuleEnabled(@CurrentCaretakerId UUID caretakerId,
      @PathVariable UUID rruleId,
      @RequestParam boolean enabled) {
    return rruleService.setEnabled(rruleId, caretakerId, enabled)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasCaretakerRole
  @DeleteMapping("/api/caretakers/me/rrules/{rruleId}")
  @Operation(summary = "Delete a caretaker rrule",
      description = "Hard-deletes the rrule and triggers slot rebuild.")
  public ResponseEntity<RRuleDto> deleteRRule(@CurrentCaretakerId UUID caretakerId,
      @PathVariable UUID rruleId) {
    return rruleService.deleteRRule(rruleId, caretakerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

}
