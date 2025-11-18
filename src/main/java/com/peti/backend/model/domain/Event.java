package com.peti.backend.model.domain;

import com.peti.backend.dto.PriceDto;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
@EqualsAndHashCode
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
  @ManyToOne
  @JoinColumn(name = "slot_id", referencedColumnName = "slot_id", nullable = false)
  private Slot slot;
  @Basic
  @Column(name = "price", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private PriceDto price;
  @Basic
  @Column(name = "status", nullable = false)
  private String status;
  @Basic
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
  @Basic
  @Column(name = "event_is_deleted", nullable = false)
  private boolean eventIsDeleted;

}
