package com.peti.backend.controller;

import com.peti.backend.dto.CityDto;
import com.peti.backend.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping("/{id}")
    public ResponseEntity<CityDto> getCityById(@PathVariable Integer id) {
        return cityService.fetchById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/country/{country}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CityDto>> getCitiesByCountry(@PathVariable String country) {
        List<CityDto> cities = cityService.fetchCitiesByCountry(country);
        return ResponseEntity.ok(cities);
    }

    @PostMapping
    public ResponseEntity<CityDto> createCity(@RequestBody CityDto cityDto) {
        CityDto createdCity = cityService.addNewCity(cityDto);
        return new ResponseEntity<>(createdCity, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CityDto> updateCity(@PathVariable Integer id, @RequestBody CityDto cityDto) {
        return cityService.modifyCity(id, cityDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
        cityService.deleteCity(id);
        return ResponseEntity.noContent().build();
    }
}