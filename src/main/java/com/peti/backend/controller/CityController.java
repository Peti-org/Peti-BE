package com.peti.backend.controller;

import com.peti.backend.dto.CityDto;
import com.peti.backend.security.annotation.HasAdminRole;
import com.peti.backend.security.annotation.HasUserRole;
import com.peti.backend.service.CityService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CityController {

  private final CityService cityService;

  @HasUserRole
  @GetMapping("/{id}")
  public ResponseEntity<CityDto> getCityById(@PathVariable Integer id) {
    return cityService.fetchById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/country/{country_code}")
  @PreAuthorize("permitAll()")
  public ResponseEntity<List<CityDto>> getCitiesByCountry(@PathVariable("country_code") String countryCode) {
    List<CityDto> cities = cityService.fetchCitiesByCountryCode(countryCode);
    return ResponseEntity.ok(cities);
  }

  @HasAdminRole
  @PostMapping
  public ResponseEntity<CityDto> createCity(@Valid @RequestBody CityDto cityDto) {
    //todo add validation for country code
    CityDto createdCity = cityService.addNewCity(cityDto);
    return new ResponseEntity<>(createdCity, HttpStatus.OK);
  }

  @HasAdminRole
  @PutMapping("/{id}")
  public ResponseEntity<CityDto> updateCity(@PathVariable Integer id, @Valid @RequestBody CityDto cityDto) {
    return cityService.modifyCity(id, cityDto)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @HasAdminRole
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCity(@PathVariable Integer id) {
    cityService.deleteCity(id);
    return ResponseEntity.noContent().build();
  }
}
