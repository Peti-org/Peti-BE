package com.peti.backend.controller;

import com.peti.backend.dto.elastic.ElasticSlotSearchRequest;
import com.peti.backend.dto.elastic.ElasticSlotSearchResponse;
import com.peti.backend.model.elastic.ElasticSlotDocument;
import com.peti.backend.service.elastic.ElasticSlotSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
@RequestMapping("/api/v2/catalog")
@Tag(name = "Catalog V2 (Elasticsearch)", description = "Elasticsearch-based catalog with flexible slots")
public class ElasticCatalogController {

  private final ElasticSlotSearchService searchService;

  // ==================== Search Endpoints ====================

  @PostMapping("/search")
  @Operation(summary = "Search available slots", 
      description = "Search for available slots with filters. Returns aggregated results grouped by caretaker.")
  public ResponseEntity<ElasticSlotSearchResponse> searchSlots(
      @Valid @RequestBody ElasticSlotSearchRequest request
  ) {
    return ResponseEntity.ok(searchService.searchSlots(request));
  }

  // ==================== Slot CRUD for Testing ====================

  @GetMapping("/slots")
  @Operation(summary = "Get all slots", description = "Get all slots from Elasticsearch (for testing)")
  public ResponseEntity<List<ElasticSlotDocument>> getAllSlots() {
    return ResponseEntity.ok(searchService.getAllSlots());
  }

  @GetMapping("/slots/{id}")
  @Operation(summary = "Get slot by ID", description = "Get a specific slot by its ID")
  public ResponseEntity<ElasticSlotDocument> getSlotById(@PathVariable String id) {
    return searchService.getSlotById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/slots/caretaker/{caretakerId}")
  @Operation(summary = "Get slots by caretaker", description = "Get all slots for a specific caretaker")
  public ResponseEntity<List<ElasticSlotDocument>> getSlotsByCaretaker(@PathVariable String caretakerId) {
    return ResponseEntity.ok(searchService.getSlotsByCaretaker(caretakerId));
  }

  @PostMapping("/slots")
  @Operation(summary = "Create slot", description = "Create a new slot in Elasticsearch (for testing)")
  public ResponseEntity<ElasticSlotDocument> createSlot(@RequestBody ElasticSlotDocument slot) {
    return ResponseEntity.ok(searchService.saveSlot(slot));
  }

  @DeleteMapping("/slots/{id}")
  @Operation(summary = "Delete slot", description = "Delete a slot from Elasticsearch")
  public ResponseEntity<Void> deleteSlot(@PathVariable String id) {
    searchService.deleteSlot(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/slots/count")
  @Operation(summary = "Count slots", description = "Get total number of slots in Elasticsearch")
  public ResponseEntity<Long> countSlots() {
    return ResponseEntity.ok(searchService.countSlots());
  }



// Скоріше за все виконуватимуться методи сервіса, але в іншому місця, як от при букінгу
    @DeleteMapping("/slots/caretaker/{caretakerId}/date/{date}")
  @Operation(summary = "Delete slots by caretaker and date", 
      description = "Delete all slots for a caretaker on a specific date (used for regeneration after booking)")
  public ResponseEntity<Void> deleteSlotsByCaretakerAndDate(
      @PathVariable String caretakerId,
      @PathVariable String date
  ) {
    searchService.deleteSlotsByCaretakerAndDate(caretakerId, LocalDate.parse(date));
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/slots/bulk")
  @Operation(summary = "Bulk create slots", 
      description = "Create multiple slots in one operation (more efficient than one-by-one)")
  public ResponseEntity<List<ElasticSlotDocument>> bulkCreateSlots(
      @RequestBody List<ElasticSlotDocument> slots
  ) {
    return ResponseEntity.ok(searchService.bulkSaveSlots(slots));
  }

  @PostMapping("/slots/caretaker/{caretakerId}/date/{date}/replace")
  @Operation(summary = "Replace slots for caretaker on date", 
      description = "Delete all slots for a caretaker on a date and create new ones. Use this after booking to regenerate slots.")
  public ResponseEntity<List<ElasticSlotDocument>> replaceSlotsByCaretakerAndDate(
      @PathVariable String caretakerId,
      @PathVariable String date,
      @RequestBody List<ElasticSlotDocument> newSlots
  ) {
    List<ElasticSlotDocument> result = searchService.replaceSlotsByCaretakerAndDate(
        caretakerId, 
        LocalDate.parse(date), 
        newSlots
    );
    return ResponseEntity.ok(result);
  }
}
