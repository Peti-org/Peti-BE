package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.pet.PetDto;
import com.peti.backend.dto.pet.RequestPetDto;
import com.peti.backend.model.Pet;
import com.peti.backend.model.User;
import com.peti.backend.repository.PetRepository;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

  @Mock
  private PetRepository petRepository;
  @Mock
  private EntityManager entityManager;

  @InjectMocks
  private PetService petService;

  @Test
  public void testGetAllPets() {
    Pet pet = ResourceLoader.loadResource("pet-entity.json", Pet.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petRepository.findAllByPetOwner_UserId(user.getUserId())).thenReturn(Collections.singletonList(pet));
    List<PetDto> result = petService.getAllPets(user);
    assertEquals(1, result.size());
    assertEquals("Rex", result.getFirst().getName());
  }

  @Test
  public void testGetPetById_Found() {
    Pet pet = ResourceLoader.loadResource("pet-entity.json", Pet.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));
    Optional<PetDto> result = petService.getPetById(pet.getPetId(), user);
    assertTrue(result.isPresent());
    assertEquals("Rex", result.get().getName());
  }

  @Test
  public void testGetPetById_NotFound() {
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petRepository.findById(petId)).thenReturn(Optional.empty());
    Optional<PetDto> result = petService.getPetById(petId, user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testCreatePet() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    Pet pet = ResourceLoader.loadResource("pet-entity.json", Pet.class);
    when(petRepository.save(any(Pet.class))).thenReturn(pet);
    PetDto result = petService.createPet(requestPetDto, user);
    assertNotNull(result);
    assertEquals("Rex", result.getName());
  }

  @Test
  public void testUpdatePet_Found() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    Pet pet = ResourceLoader.loadResource("pet-entity.json", Pet.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));
    when(petRepository.save(any(Pet.class))).thenReturn(pet);
    Optional<PetDto> result = petService.updatePet(pet.getPetId(), requestPetDto, user);
    assertTrue(result.isPresent());
    assertEquals("Rex", result.get().getName());
  }

  @Test
  public void testUpdatePet_NotFound() {
    RequestPetDto requestPetDto = ResourceLoader.loadResource("pet-request.json", RequestPetDto.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petRepository.findById(petId)).thenReturn(Optional.empty());
    Optional<PetDto> result = petService.updatePet(petId, requestPetDto, user);
    assertFalse(result.isPresent());
  }

  @Test
  public void testDeletePet_Found() {
    Pet pet = ResourceLoader.loadResource("pet-entity.json", Pet.class);
    User user = ResourceLoader.loadResource("user.json", User.class);
    when(petRepository.findById(pet.getPetId())).thenReturn(Optional.of(pet));
    Optional<PetDto> result = petService.deletePet(pet.getPetId(), user);
    assertTrue(result.isPresent());
    assertEquals("Rex", result.get().getName());
  }

  @Test
  public void testDeletePet_NotFound() {
    User user = ResourceLoader.loadResource("user.json", User.class);
    UUID petId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    when(petRepository.findById(petId)).thenReturn(Optional.empty());
    Optional<PetDto> result = petService.deletePet(petId, user);
    assertFalse(result.isPresent());
  }
}

