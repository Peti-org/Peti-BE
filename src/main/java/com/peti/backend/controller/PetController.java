package com.peti.backend.controller;

import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.User;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.PetService;
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
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "Operations for managing pets for specific user")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

  private final PetService petService;

  @HasUserRole
  @PostMapping
  public ResponseEntity<PetDto> createPet(@Valid @RequestBody RequestPetDto requestPetDto,
      @Parameter(hidden = true) User user) {
    PetDto createdPet = petService.createPet(requestPetDto, user);
    return ResponseEntity.ok(createdPet);
  }

  @HasUserRole
  @GetMapping
  public ResponseEntity<List<PetDto>> getAllPets(@Parameter(hidden = true) User user) {
    return ResponseEntity.ok(petService.getAllPets(user));
  }

  @HasUserRole
  @GetMapping("/{id}")
  public ResponseEntity<PetDto> getPetById(@PathVariable UUID id, @Parameter(hidden = true) User user) {
    return petService.getPetById(id, user)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @PutMapping("/{id}")
  public ResponseEntity<PetDto> updatePet(@PathVariable UUID id, @Valid @RequestBody RequestPetDto petDto,
      @Parameter(hidden = true) User user) {
    return petService.updatePet(id, petDto, user)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @DeleteMapping("/{id}")
  public ResponseEntity<PetDto> deletePet(@PathVariable UUID id, @Parameter(hidden = true) User user) {
    return petService.deletePet(id, user)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @ModelAttribute("user")
  public User getUser(Authentication authentication) {
    try {
      return (User) authentication.getPrincipal();
    } catch (ClassCastException e) {
      throw new IllegalArgumentException("Authentication is wrong");
    }
  }
}
