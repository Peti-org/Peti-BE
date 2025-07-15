package com.peti.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.BreedDto;
import com.peti.backend.service.BreedService;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
public class BreedControllerTest {

  @Mock
  private BreedService breedService;

  @InjectMocks
  private BreedController breedController;

  @Test
  public void testGetAllBreeds() {
    List<BreedDto> breeds = Collections.singletonList(
        ResourceLoader.loadResource("breed-response.json", BreedDto.class));
    when(breedService.getAllBreeds()).thenReturn(breeds);
    ResponseEntity<List<BreedDto>> response = breedController.getAllBreeds();
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(1, response.getBody().size());
  }

  @Test
  public void testCreateBreed() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    BreedDto createdBreed = ResourceLoader.loadResource("breed-response.json", BreedDto.class);
    when(breedService.createBreed(any(BreedDto.class))).thenReturn(createdBreed);
    ResponseEntity<BreedDto> response = breedController.createBreed(breedDto);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(createdBreed.getId(), response.getBody().getId());
  }

  @Test
  public void testUpdateBreed_Found() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    BreedDto updatedBreed = ResourceLoader.loadResource("breed-response.json", BreedDto.class);
    when(breedService.updateBreed(eq(1), any(BreedDto.class))).thenReturn(Optional.of(updatedBreed));
    ResponseEntity<BreedDto> response = breedController.updateBreed(1, breedDto);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(updatedBreed.getId(), response.getBody().getId());
  }

  @Test
  public void testUpdateBreed_NotFound() {
    BreedDto breedDto = ResourceLoader.loadResource("breed-request.json", BreedDto.class);
    when(breedService.updateBreed(eq(99), any(BreedDto.class))).thenReturn(Optional.empty());
    ResponseEntity<BreedDto> response = breedController.updateBreed(99, breedDto);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }

  @Test
  public void testDeleteBreed_Found() {
    BreedDto deletedBreed = ResourceLoader.loadResource("breed-response.json", BreedDto.class);
    when(breedService.deleteBreed(1)).thenReturn(Optional.of(deletedBreed));
    ResponseEntity<BreedDto> response = breedController.deleteBreed(1);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(deletedBreed.getId(), response.getBody().getId());
  }

  @Test
  public void testDeleteBreed_NotFound() {
    when(breedService.deleteBreed(99)).thenReturn(Optional.empty());
    ResponseEntity<BreedDto> response = breedController.deleteBreed(99);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertNull(response.getBody());
  }
}

