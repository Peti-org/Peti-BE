package com.peti.backend.model.domain;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "caretaker_slot", schema = "peti", catalog = "peti")
@EqualsAndHashCode
public class CaretakerSlot {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "slot_id ", nullable = false)
  private UUID slotId;

  @ManyToOne
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretaker;

  @Basic
  @Column(name = "date", nullable = false)
  private Date date;

  @Basic
  @Column(name = "time_from", nullable = false)
  private Time timeFrom;

  @Basic
  @Column(name = "time_to", nullable = false)
  private Time timeTo;

  @Basic
  @Column(name = "type", nullable = false)
  private String type;

  @Basic
  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Basic
  @Column(name = "currency", nullable = false)
  private String currency;

  @Basic
  @Column(name = "additional_data", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object additionalData;

}
