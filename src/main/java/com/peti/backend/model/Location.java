package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "location", schema = "peti", catalog = "peti")
@EqualsAndHashCode
public class Location {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "location_id", nullable = false)
  private int locationId;
  @Basic
  @Column(name = "longitude", precision = 8)
  private BigDecimal longitude;
  @Basic
  @Column(name = "latitude", precision = 8)
  private BigDecimal latitude;
  @Basic
  @Column(name = "address", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object address;
}
