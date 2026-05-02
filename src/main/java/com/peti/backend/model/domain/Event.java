package com.peti.backend.model.domain;

import com.peti.backend.dto.PriceDto;
import com.peti.backend.model.internal.EventStatus;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "event", schema = "peti", catalog = "peti")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "event_id", nullable = false)
  @EqualsAndHashCode.Include
  private UUID eventId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretaker;

  /** The recurring rule this event was booked from. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rrule_id", referencedColumnName = "rrule_id", nullable = false)
  private CaretakerRRule rrule;

  @Basic
  @Column(name = "price", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private PriceDto price;

  @Basic
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private EventStatus status;

  @Basic
  @Column(name = "type", nullable = false)
  private String type;

  @Basic
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Basic
  @Column(name = "datetime_from", nullable = false)
  private LocalDateTime datetimeFrom;

  @Basic
  @Column(name = "datetime_to", nullable = false)
  private LocalDateTime datetimeTo;

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "event_pets",
      schema = "peti",
      joinColumns = @JoinColumn(name = "event_id"),
      inverseJoinColumns = @JoinColumn(name = "pet_id"))
  private Set<Pet> pets = new HashSet<>();
}

