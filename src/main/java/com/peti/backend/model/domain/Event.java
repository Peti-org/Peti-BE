package com.peti.backend.model.domain;

import com.peti.backend.dto.PriceDto;
import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Setter
@Getter
@Table(name = "event", schema = "peti", catalog = "peti")
@EqualsAndHashCode(exclude = "slots")
public class Event {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "event_id", nullable = false)
  private UUID eventId;
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User user;
  @ManyToOne
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretaker;
  @Basic
  @Column(name = "price", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private PriceDto price;
  @Basic
  @Column(name = "status", nullable = false)
  private String status;
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
  @Basic
  @Column(name = "event_is_deleted", nullable = false)
  private boolean eventIsDeleted;

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "event_slots",
      schema = "peti",
      joinColumns = @JoinColumn(name = "event_id"),
      inverseJoinColumns = @JoinColumn(name = "slot_id"))
  private Set<Slot> slots = new HashSet<>();

  @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(name = "event_pets",
      schema = "peti",
      joinColumns = @JoinColumn(name = "event_id"),
      inverseJoinColumns = @JoinColumn(name = "pet_id"))
  private Set<Pet> pets = new HashSet<>();
}
