package com.peti.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.CityDto;
import com.peti.backend.model.City;
import com.peti.backend.repository.CityRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

  @Mock
  private CityRepository cityRepository;

  @InjectMocks
  private CityService cityService;

  @Test
  public void testFetchById_Found() {
    City city = ResourceLoader.loadResource("city-entity.json", City.class);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
    Optional<CityDto> result = cityService.fetchById(1L);
    assertTrue(result.isPresent());
    assertEquals(1, result.get().getId());
    assertEquals("Kyiv", result.get().getCity());
  }

  @Test
  public void testFetchById_NotFound() {
    when(cityRepository.findById(99L)).thenReturn(Optional.empty());
    Optional<CityDto> result = cityService.fetchById(99L);
    assertFalse(result.isPresent());
  }

  @Test
  public void testFetchCitiesByCountryCode() {
    City city = ResourceLoader.loadResource("city-entity.json", City.class);
    when(cityRepository.findByCountryCode("UA")).thenReturn(Collections.singletonList(city));
    List<CityDto> result = cityService.fetchCitiesByCountryCode("ua");
    assertEquals(1, result.size());
    assertEquals("Kyiv", result.getFirst().getCity());
  }

  @Test
  public void testAddNewCity() {
    CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
    City city = ResourceLoader.loadResource("city-entity.json", City.class);
    when(cityRepository.save(any(City.class))).thenReturn(city);
    CityDto result = cityService.addNewCity(cityDto);
    assertNotNull(result);
    assertEquals(cityDto.getCity(), result.getCity());
  }

  @Test
  public void testModifyCity_Found() {
    CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
    City city = ResourceLoader.loadResource("city-entity.json", City.class);
    when(cityRepository.findById(1L)).thenReturn(Optional.of(city));
    when(cityRepository.save(any(City.class))).thenReturn(city);
    Optional<CityDto> result = cityService.modifyCity(1L, cityDto);
    assertTrue(result.isPresent());
    assertEquals(cityDto.getCity(), result.get().getCity());
  }

  @Test
  public void testModifyCity_NotFound() {
    CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
    when(cityRepository.findById(99L)).thenReturn(Optional.empty());
    Optional<CityDto> result = cityService.modifyCity(99L, cityDto);
    assertFalse(result.isPresent());
  }

  @Test
  public void testDeleteCity() {
    doNothing().when(cityRepository).deleteById(1L);
    cityService.deleteCity(1L);
    verify(cityRepository).deleteById(1L);
  }
}
