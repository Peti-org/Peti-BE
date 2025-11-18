package com.peti.backend.model.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@Table(name = "caretaker_slot", schema = "peti", catalog = "peti")
@EqualsAndHashCode
public class Slot {

  @GeneratedValue(strategy = GenerationType.UUID)
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
  @Column(name = "creation_time", nullable = false)
  private LocalDateTime creationTime;

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

  @Basic
  @Column(name = "is_available", nullable = false)
  private boolean available;
}
