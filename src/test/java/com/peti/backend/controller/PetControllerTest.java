package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.domain.User;
import com.peti.backend.model.projection.UserProjection;
import com.peti.backend.service.PetService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
public class PetControllerTest {

  @Mock
  private PetService petService;

  @InjectMocks
  private PetController petController;

  @Test
  public void testCreatePet() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    when(petService.createPet(any(RequestPetDto.class), any(UserProjection.class))).thenReturn(petDto);
    ResponseEntity<PetDto> response = petController.createPet(requestPetDto, userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testGetAllPets() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    when(petService.getAllPets(any(UserProjection.class))).thenReturn(Collections.singletonList(petDto));
    ResponseEntity<List<PetDto>> response = petController.getAllPets(userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void testGetPetById_Found() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.getPetById(eq(petId), any(UserProjection.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.getPetById(petId, userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testGetPetById_NotFound() {
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.getPetById(eq(petId), any(UserProjection.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.getPetById(petId, userProjection);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testUpdatePet_Found() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.updatePet(eq(petId), any(RequestPetDto.class), any(UserProjection.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.updatePet(petId, requestPetDto, userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testUpdatePet_NotFound() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.updatePet(eq(petId), any(RequestPetDto.class), any(UserProjection.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.updatePet(petId, requestPetDto, userProjection);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testDeletePet_Found() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.deletePet(eq(petId), any(UserProjection.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.deletePet(petId, userProjection);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testDeletePet_NotFound() {
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.deletePet(eq(petId), any(UserProjection.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.deletePet(petId, userProjection);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }


  @Test
  public void testGetUser_ValidAuthentication() {
    UserProjection userProjection = ResourceLoader.loadResource("user-projection-entity.json", UserProjection.class);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(userProjection);

    PetController controller = new PetController(petService);
    UserProjection result = controller.getUserProjection(authentication);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(userProjection.getUserId(), result.getUserId());
  }

  @Test
  public void testGetUser_InvalidAuthentication() {
    Authentication authentication = mock(Authentication.class);
    org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn("not a user");

    PetController controller = new PetController(petService);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      controller.getUserProjection(authentication);
    });
  }
}

