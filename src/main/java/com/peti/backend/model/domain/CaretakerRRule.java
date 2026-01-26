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
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "caretaker_rrule", schema = "peti", catalog = "peti")
@EqualsAndHashCode
public class CaretakerRRule {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "rrule_id", nullable = false)
  private UUID rruleId;

  @ManyToOne
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretaker;

  @Basic
  @Column(name = "rrule", nullable = false, length = 500)
  private String rrule;

  @Basic
  @Column(name = "dtstart", nullable = false)
  private LocalDateTime dtstart;

  @Basic
  @Column(name = "dtend")
  private LocalDateTime dtend;

  @Basic
  @Column(name = "description", length = 255)
  private String description;

  @Basic
  @Column(name = "slot_type", nullable = false, length = 50)
  private String slotType;

  @Basic
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}

