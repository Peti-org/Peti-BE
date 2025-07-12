package com.peti.backend.service;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.CityDto;
import com.peti.backend.model.City;
import com.peti.backend.repository.CityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    @Test
    public void testFetchById_Found() {
        City city = ResourceLoader.loadResource("city-entity.json", City.class);
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        Optional<CityDto> result = cityService.fetchById(1);
        assertTrue(result.isPresent());
        assertEquals(1, result.get().getId());
        assertEquals("Kyiv", result.get().getCity());
    }

    @Test
    public void testFetchById_NotFound() {
        when(cityRepository.findById(99)).thenReturn(Optional.empty());
        Optional<CityDto> result = cityService.fetchById(99);
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
        when(cityRepository.findById(1)).thenReturn(Optional.of(city));
        when(cityRepository.save(any(City.class))).thenReturn(city);
        Optional<CityDto> result = cityService.modifyCity(1, cityDto);
        assertTrue(result.isPresent());
        assertEquals(cityDto.getCity(), result.get().getCity());
    }

    @Test
    public void testModifyCity_NotFound() {
        CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
        when(cityRepository.findById(99)).thenReturn(Optional.empty());
        Optional<CityDto> result = cityService.modifyCity(99, cityDto);
        assertFalse(result.isPresent());
    }

    @Test
    public void testDeleteCity() {
        doNothing().when(cityRepository).deleteById(1);
        cityService.deleteCity(1);
        verify(cityRepository).deleteById(1);
    }
}
