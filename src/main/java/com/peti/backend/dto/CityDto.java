package com.peti.backend.dto;

import com.peti.backend.model.City;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CityDto {

  private Integer id;
  @NotEmpty(message = "City cannot be blank")
  private String city;
  @NotEmpty(message = "Country cannot be blank")
  private String country;
  @NotEmpty(message = "Country code cannot be blank")
  @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must consist of 2 uppercase letters")
  private String countryCode;
  private String locationInfo;
  private BigDecimal longitude;
  private BigDecimal latitude;

  public City toCity() {
    City cityEntity = new City();
    cityEntity.setCity(city);
    cityEntity.setCountry(country);
    cityEntity.setCountryCode(countryCode);
    cityEntity.setLocationInfo(locationInfo);
    cityEntity.setLongitude(longitude);
    cityEntity.setLatitude(latitude);
    return cityEntity;
  }

  public City toCityWithId() {
    City cityEntity = new City();
    cityEntity.setCityId(id);
    return cityEntity;
  }
}
