package com.peti.backend.dto;

import com.peti.backend.model.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class LocationDto {
    private Long id;
    private String address;
    private String city;
    private String country;
    private String postalCode;

    public Location toLocation() {
        Location location = new Location();
//        location.setAddress(address); //TODO: fix address set up in future
//        location.setCity(city);
//        location.setCountry(country);
        return location;
    }
}
