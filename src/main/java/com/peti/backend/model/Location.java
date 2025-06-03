package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "location", schema = "peti", catalog = "peti")
public class Location {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "location_id", nullable = false)
  private int locationId;
  @Basic
  @Column(name = "longitude", nullable = true, precision = 8)
  private BigDecimal longitude;
  @Basic
  @Column(name = "latitude", nullable = true, precision = 8)
  private BigDecimal latitude;
  @Basic
  @Column(name = "country", nullable = false, length = 20)
  private String country;
  @Basic
  @Column(name = "city", nullable = false, length = 40)
  private String city;
  @Basic
  @Column(name = "address", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object address;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Location location = (Location) o;

    if (locationId != location.locationId)
      return false;
    if (longitude != null ? !longitude.equals(location.longitude) : location.longitude != null)
      return false;
    if (latitude != null ? !latitude.equals(location.latitude) : location.latitude != null)
      return false;
    if (country != null ? !country.equals(location.country) : location.country != null)
      return false;
    if (city != null ? !city.equals(location.city) : location.city != null)
      return false;
    if (address != null ? !address.equals(location.address) : location.address != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = locationId;
    result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
    result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
    result = 31 * result + (country != null ? country.hashCode() : 0);
    result = 31 * result + (city != null ? city.hashCode() : 0);
    result = 31 * result + (address != null ? address.hashCode() : 0);
    return result;
  }
}
