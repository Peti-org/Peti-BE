package com.peti.backend.service;

import com.peti.backend.dto.CityDto;
import com.peti.backend.model.City;
import com.peti.backend.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CityService {

  private final CityRepository cityRepository;

  public Optional<CityDto> fetchById(Integer id) {
    return cityRepository.findById(id).map(CityService::convertToDto);
  }

  public List<CityDto> fetchCitiesByCountryCode(String countryCode) {
    String formattedCountryCode = countryCode.toUpperCase();
    return cityRepository.findByCountryCode(formattedCountryCode).stream()
            .map(CityService::convertToDto)
            .collect(Collectors.toList());
  }

  public CityDto addNewCity(CityDto cityDto) {
    City savedCity = cityRepository.save(cityDto.toCity());
    return convertToDto(savedCity);
  }

  public Optional<CityDto> modifyCity(Integer id, CityDto cityDto) {
    return cityRepository.findById(id)
            .map(existingCity -> {
              existingCity.setCity(cityDto.getCity());
              existingCity.setCountry(cityDto.getCountry());
              existingCity.setLocationInfo(cityDto.getLocationInfo());
              // Only update coordinates if they are provided
              if (cityDto.getLongitude() != null) {
                existingCity.setLongitude(cityDto.getLongitude());
              }
              if (cityDto.getLatitude() != null) {
                existingCity.setLatitude(cityDto.getLatitude());
              }
              return cityRepository.save(existingCity);
            })
            .map(CityService::convertToDto);
  }

  public void deleteCity(Integer id) {
    cityRepository.deleteById(id);
  }

  public static CityDto convertToDto(City city) {
    return new CityDto(
            city.getCityId(),
            city.getCity(),
            city.getCountry(),
            city.getLocationInfo(),
            city.getLongitude(),
            city.getLatitude()
    );
  }

}
