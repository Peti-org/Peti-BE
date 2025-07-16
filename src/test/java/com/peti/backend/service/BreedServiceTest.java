package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.BreedDto;
import com.peti.backend.model.domain.Breed;
import com.peti.backend.repository.BreedRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class BreedServiceTest {

  @Mock
  private BreedRepository breedRepository;

  @InjectMocks
  private BreedService breedService;

  @Test
  public void testGetAllBreeds() {
    Breed breed = ResourceLoader.loadResource("breed-entity.json", Breed.class);
    when(breedRepository.findAll()).thenReturn(Collections.singletonList(breed));
    List<BreedDto> result = breedService.getAllBreeds();
    assertEquals(1, result.size());
    assertEquals("Labrador Retriever", result.getFirst().getBreedName());
  }

  @Test
  public void testCreateBreed() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    Breed breed = ResourceLoader.loadResource("breed-entity.json", Breed.class);
    when(breedRepository.save(any(Breed.class))).thenReturn(breed);
    BreedDto result = breedService.createBreed(breedDto);
    assertNotNull(result);
    assertEquals(breedDto.getBreedName(), result.getBreedName());
  }

  @Test
  public void testUpdateBreed_Found() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    Breed breed = ResourceLoader.loadResource("breed-entity.json", Breed.class);
    when(breedRepository.findById(1)).thenReturn(Optional.of(breed));
    when(breedRepository.save(any(Breed.class))).thenReturn(breed);
    Optional<BreedDto> result = breedService.updateBreed(1, breedDto);
    assertTrue(result.isPresent());
    assertEquals(breedDto.getBreedName(), result.get().getBreedName());
  }

  @Test
  public void testUpdateBreed_NotFound() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    when(breedRepository.findById(99)).thenReturn(Optional.empty());
    Optional<BreedDto> result = breedService.updateBreed(99, breedDto);
    assertFalse(result.isPresent());
  }

  @Test
  public void testDeleteBreed_Found() {
    Breed breed = ResourceLoader.loadResource("breed-entity.json", Breed.class);
    when(breedRepository.findById(1)).thenReturn(Optional.of(breed));
    doNothing().when(breedRepository).deleteById(1);
    Optional<BreedDto> result = breedService.deleteBreed(1);
    assertTrue(result.isPresent());
    assertEquals("Labrador Retriever", result.get().getBreedName());
  }

  @Test
  public void testDeleteBreed_NotFound() {
    when(breedRepository.findById(99)).thenReturn(Optional.empty());
    Optional<BreedDto> result = breedService.deleteBreed(99);
    assertFalse(result.isPresent());
  }
}

