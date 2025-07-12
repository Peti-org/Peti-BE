package com.peti.backend.controller;

import com.peti.backend.ResourceLoader;
import com.peti.backend.dto.CityDto;
import com.peti.backend.service.CityService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CityControllerTest {

    @Mock
    private CityService cityService;

    @InjectMocks
    private CityController cityController;

    @Test
    public void testGetCityById_Found() {
        CityDto cityDto = ResourceLoader.loadResource("city-response.json", CityDto.class);
        when(cityService.fetchById(1)).thenReturn(Optional.of(cityDto));
        ResponseEntity<CityDto> response = cityController.getCityById(1);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cityDto.getId(), response.getBody().getId());
    }

    @Test
    public void testGetCityById_NotFound() {
        when(cityService.fetchById(99)).thenReturn(Optional.empty());
        ResponseEntity<CityDto> response = cityController.getCityById(99);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testGetCitiesByCountry() {
        List<CityDto> cities = Arrays.asList(ResourceLoader.loadResource("city-response.json", CityDto.class));
        when(cityService.fetchCitiesByCountryCode("UA")).thenReturn(cities);
        ResponseEntity<List<CityDto>> response = cityController.getCitiesByCountry("UA");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testCreateCity() {
        CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
        CityDto createdCity = ResourceLoader.loadResource("city-response.json", CityDto.class);
        when(cityService.addNewCity(any(CityDto.class))).thenReturn(createdCity);
        ResponseEntity<CityDto> response = cityController.createCity(cityDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(createdCity.getId(), response.getBody().getId());
    }

    @Test
    public void testUpdateCity_Found() {
        CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
        CityDto updatedCity = ResourceLoader.loadResource("city-response.json", CityDto.class);
        when(cityService.modifyCity(eq(1), any(CityDto.class))).thenReturn(Optional.of(updatedCity));
        ResponseEntity<CityDto> response = cityController.updateCity(1, cityDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedCity.getId(), response.getBody().getId());
    }

    @Test
    public void testUpdateCity_NotFound() {
        CityDto cityDto = ResourceLoader.loadResource("city-request.json", CityDto.class);
        when(cityService.modifyCity(eq(99), any(CityDto.class))).thenReturn(Optional.empty());
        ResponseEntity<CityDto> response = cityController.updateCity(99, cityDto);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDeleteCity() {
        doNothing().when(cityService).deleteCity(1);
        ResponseEntity<Void> response = cityController.deleteCity(1);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(cityService).deleteCity(1);
    }
}

