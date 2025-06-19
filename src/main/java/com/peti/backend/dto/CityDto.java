package com.peti.backend.dto;

import com.peti.backend.model.City;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CityDto {
    private Integer id;
    private String city;
    private String country;
    private String locationInfo;
    private BigDecimal longitude;
    private BigDecimal latitude;

    public City toCity() {
        City cityEntity = new City();
        cityEntity.setCity(city);
        cityEntity.setCountry(country);
        cityEntity.setLocationInfo(locationInfo);
        cityEntity.setLongitude(longitude);
        cityEntity.setLatitude(latitude);
        return cityEntity;
    }
}
