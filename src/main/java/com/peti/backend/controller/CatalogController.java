package com.peti.backend.controller;

import com.peti.backend.dto.caretacker.CaretakerDto;
import com.peti.backend.dto.slot.PagedSlotsResponse;
import com.peti.backend.dto.slot.RequestSlotDto;
import com.peti.backend.dto.slot.RequestSlotFilters;
import com.peti.backend.dto.slot.SlotDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.HasCaretakerRole;
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
//@SecurityRequirement(name = "bearerAuth")
public class CatalogController {

  private final SlotService slotService;

  @PostMapping("/filter")
  public ResponseEntity<PagedSlotsResponse> getFilteredEvents(@Valid @RequestBody RequestSlotFilters request) {
    return ResponseEntity.ok(slotService.getFilteredSlots(request));
  }

  @GetMapping("/{slotId}")
  public ResponseEntity<SlotDto> getSlotById(@PathVariable UUID slotId) {
    return slotService.getSlotById(slotId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
