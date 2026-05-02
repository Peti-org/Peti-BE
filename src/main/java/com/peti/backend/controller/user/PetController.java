package com.peti.backend.controller.user;

import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.security.annotation.CurrentUser;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.user.PetService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/pets")
@RequiredArgsConstructor
@Tag(name = "Pet", description = "Operations for managing pets for specific user")
@SecurityRequirement(name = "bearerAuth")
public class PetController {

  private final PetService petService;

  @HasUserRole
  @PostMapping
  public ResponseEntity<PetDto> createPet(@Valid @RequestBody RequestPetDto requestPetDto,
      @CurrentUser UserProjection userProjection) {
    PetDto createdPet = petService.createPet(requestPetDto, userProjection);
    return ResponseEntity.ok(createdPet);
  }

  @HasUserRole
  @GetMapping
  public ResponseEntity<List<PetDto>> getAllPets(@CurrentUser UserProjection userProjection) {
    return ResponseEntity.ok(petService.getAllPets(userProjection));
  }

  @HasUserRole
  @GetMapping("/{id}")
  public ResponseEntity<PetDto> getPetById(@PathVariable UUID id, @CurrentUser UserProjection userProjection) {
    return petService.getPetById(id, userProjection)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @PutMapping("/{id}")
  public ResponseEntity<PetDto> updatePet(@PathVariable UUID id, @Valid @RequestBody RequestPetDto petDto,
      @CurrentUser UserProjection userProjection) {
    return petService.updatePet(id, petDto, userProjection)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasUserRole
  @DeleteMapping("/{id}")
  public ResponseEntity<PetDto> deletePet(@PathVariable UUID id, @CurrentUser UserProjection userProjection) {
    return petService.deletePet(id, userProjection)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}
