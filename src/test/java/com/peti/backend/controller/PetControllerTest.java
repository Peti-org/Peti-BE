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
import com.peti.backend.model.User;
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
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petService.createPet(any(RequestPetDto.class), any(User.class))).thenReturn(petDto);
    ResponseEntity<PetDto> response = petController.createPet(requestPetDto, user);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testGetAllPets() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petService.getAllPets(any(User.class))).thenReturn(Collections.singletonList(petDto));
    ResponseEntity<List<PetDto>> response = petController.getAllPets(user);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void testGetPetById_Found() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.getPetById(eq(petId), any(User.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.getPetById(petId, user);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testGetPetById_NotFound() {
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.getPetById(eq(petId), any(User.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.getPetById(petId, user);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testUpdatePet_Found() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.updatePet(eq(petId), any(RequestPetDto.class), any(User.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.updatePet(petId, requestPetDto, user);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testUpdatePet_NotFound() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.updatePet(eq(petId), any(RequestPetDto.class), any(User.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.updatePet(petId, requestPetDto, user);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testDeletePet_Found() {
    PetDto petDto = ResourceLoader.loadResource("pet-response.json", PetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.deletePet(eq(petId), any(User.class))).thenReturn(Optional.of(petDto));
    ResponseEntity<PetDto> response = petController.deletePet(petId, user);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Assertions.assertNotNull(response.getBody());
    assertEquals(petDto.getName(), response.getBody().getName());
  }

  @Test
  public void testDeletePet_NotFound() {
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petService.deletePet(eq(petId), any(User.class))).thenReturn(Optional.empty());
    ResponseEntity<PetDto> response = petController.deletePet(petId, user);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }


  @Test
  public void testGetUser_ValidAuthentication() {
    User user = ResourceLoader.loadResource("user.json", User.class);
    Authentication authentication = mock(Authentication.class);
    when(authentication.getPrincipal()).thenReturn(user);

    PetController controller = new PetController(petService);
    User result = controller.getUser(authentication);

    Assertions.assertNotNull(result);
    Assertions.assertEquals(user.getUserId(), result.getUserId());
  }

  @Test
  public void testGetUser_InvalidAuthentication() {
    Authentication authentication = mock(Authentication.class);
    org.mockito.Mockito.when(authentication.getPrincipal()).thenReturn("not a user");

    PetController controller = new PetController(petService);

    Assertions.assertThrows(IllegalArgumentException.class, () -> {
      controller.getUser(authentication);
    });
  }
}

