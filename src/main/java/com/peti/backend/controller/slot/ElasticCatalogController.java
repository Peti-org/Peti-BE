package com.peti.backend.controller.slot;

import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchResponse;
import com.peti.backend.service.elastic.ElasticSlotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/catalog")
@Tag(name = "Catalog V2 (Elasticsearch)", description = "Elasticsearch-based catalog with flexible slots")
public class ElasticCatalogController {

  private final ElasticSlotSearchService searchService;

  @PostMapping("/search")
  @Operation(summary = "Search available slots",
      description = "Search for available slots with filters. Returns aggregated results grouped by caretaker.")
  public ResponseEntity<ElasticSlotSearchResponse> searchSlots(@Valid @RequestBody ElasticSlotSearchRequest request) {
    return ResponseEntity.ok(searchService.searchSlots(request));
  }
}
