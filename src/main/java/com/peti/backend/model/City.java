package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "city", schema = "peti", catalog = "peti")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class City {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "city_id", nullable = false)
  private int cityId;

  @Basic
  @Column(name = "longitude", precision = 8)
  private BigDecimal longitude;

  @Basic
  @Column(name = "latitude", precision = 8)
  private BigDecimal latitude;

  @Basic
  @Column(name = "country", nullable = false, length = 20)
  private String country;

  @Basic
  @Column(name = "country_code", nullable = false, length = 20)
  private String countryCode;

  @Basic
  @Column(name = "city", nullable = false, length = 40)
  private String city;

  @Basic
  @Column(name = "location_info", length = 100)
  private String locationInfo;
}
