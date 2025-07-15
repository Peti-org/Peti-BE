package com.peti.backend.dto;

import com.peti.backend.model.City;
import io.swagger.v3.oas.annotations.media.Schema;
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

  private Long id;
  @NotEmpty(message = "City cannot be blank")
  @Schema(description = "City name", defaultValue = "Berlin")
  private String city;
  @NotEmpty(message = "Country cannot be blank")
  @Schema(description = "Country name", defaultValue = "Germany")
  private String country;
  @NotEmpty(message = "Country code cannot be blank")
  @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must consist of 2 uppercase letters")
  @Schema(description = "Country code", defaultValue = "DE")
  private String countryCode;
  @Schema(description = "Country code", defaultValue = "This is a beautiful city in western Germany near the Rhine River", nullable  = true)
  private String locationInfo;
  @Schema(description = "Longitude of city", defaultValue = "22.04620000", nullable  = true)
  private BigDecimal longitude;
  @Schema(description = "Latitude of city", defaultValue = "48.46470000", nullable  = true)
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
