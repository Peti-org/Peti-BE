package com.peti.backend.controller;

import com.peti.backend.dto.BreedDto;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.BreedService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/breeds")
@RequiredArgsConstructor
@Tag(name = "Breed", description = "Operations for managing breeds")
@SecurityRequirement(name = "bearerAuth")
public class BreedController {

  private final BreedService breedService;

  @HasUserRole
  @GetMapping
  public ResponseEntity<List<BreedDto>> getAllBreeds() {
    return ResponseEntity.ok(breedService.getAllBreeds());
  }

  @HasAdminRole
  @PostMapping
  public ResponseEntity<BreedDto> createBreed(@Valid @RequestBody BreedDto breedDto) {
    return ResponseEntity.ok(breedService.createBreed(breedDto));
  }

  @HasAdminRole
  @PutMapping("/{id}")
  public ResponseEntity<BreedDto> updateBreed(@PathVariable Integer id, @Valid @RequestBody BreedDto breedDto) {
    return breedService.updateBreed(id, breedDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasAdminRole
  @DeleteMapping("/{id}")
  public ResponseEntity<BreedDto> deleteBreed(@PathVariable Integer id) {
    return breedService.deleteBreed(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
