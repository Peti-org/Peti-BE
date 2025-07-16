package com.peti.backend.service;

import com.peti.backend.dto.LocationDto;
import com.peti.backend.model.domain.Location;
import com.peti.backend.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public List<LocationDto> getAllLocations() {
        return locationRepository.findAll().stream()
                .map(LocationMapper::toDto)
                .toList();
    }

    public Optional<LocationDto> getLocationById(Long id) {
        return locationRepository.findById(id).map(LocationMapper::toDto);
    }

    public LocationDto createLocation(Location location) {
        Location savedLocation = locationRepository.save(location);
        return LocationMapper.toDto(savedLocation);
    }

    public Optional<LocationDto> updateLocation(Long id, Location locationDetails) {
        return locationRepository.findById(id)
                .map(existingLocation -> {
//                    existingLocation.setCity(locationDetails.getCity());
//                    existingLocation.setCountry(locationDetails.getCountry());
                    return existingLocation;
                })
                .map(locationRepository::save)
                .map(LocationMapper::toDto);
    }

    public void deleteLocation(Long id) {
        locationRepository.deleteById(id);
    }
    public static class LocationMapper {
        public static LocationDto toDto(Location location) {
            return new LocationDto((long) location.getLocationId(), "1", "1", "1", "1");
        }

    }
}
