package com.peti.backend.controller;

import com.peti.backend.dto.CaretakerDto;
import com.peti.backend.model.Caretaker;
import com.peti.backend.service.CaretakerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/caretakers")
public class CaretakerController {

    @Autowired
    private CaretakerService caretakerService;

    @GetMapping
    public ResponseEntity<List<CaretakerDto>> getAllCaretakers() {
        return ResponseEntity.ok(caretakerService.getAllCaretakers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaretakerDto> getCaretakerById(@PathVariable Long id) {
        return caretakerService.getCaretakerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CaretakerDto> createCaretaker(@RequestBody CaretakerDto caretakerDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caretakerService.createCaretaker(caretakerDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaretakerDto> updateCaretaker(@PathVariable Long id, @RequestBody CaretakerDto caretakerDto) {
        return ResponseEntity.ok(caretakerService.updateCaretaker(id, caretakerDto));
    }

    @DeleteMapping("/{caretakerId}")
    public ResponseEntity<Void> deleteCaretaker(@PathVariable("caretakerId") Long caretakerId) {
        caretakerService.deleteCaretaker(caretakerId);
        return ResponseEntity.noContent().build();
    }
}