package com.peti.backend.controller;

import com.peti.backend.dto.CaretakerSlotDto;
import com.peti.backend.service.CaretakerSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/slots")
public class CaretackerSlotController {

  private final CaretakerSlotService slotService;

  @PostMapping
  public ResponseEntity<List<CaretakerSlotDto>> getAllEvents() {
    return ResponseEntity.ok(slotService.getAllEvents());
  }

  @GetMapping("/{id}")
  public ResponseEntity<CaretakerSlotDto> getEventById(@PathVariable UUID id) {
    return slotService.getEventById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
  }

  //    @PostMapping
  //    public ResponseEntity<EventDto> createEvent(@RequestBody EventDto eventDto) {
  //        return new ResponseEntity<>(eventService.createEvent(eventDto), HttpStatus.CREATED);
  //    }

  //    @PutMapping("/{id}")
  //    public ResponseEntity<EventDto> updateEvent(@PathVariable Long id, @RequestBody EventDto eventDto) {
  //        return eventService.updateEvent(id, eventDto)
  //                .map(ResponseEntity::ok)
  //                .orElse(ResponseEntity.notFound().build());
  //    }
  //
  //    @DeleteMapping("/{id}")
  //    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
  //        if (eventService.deleteEvent(id)) {
  //            return ResponseEntity.noContent().build();
  //        } else {
  //            return ResponseEntity.notFound().build();
  //        }
  //    }
}