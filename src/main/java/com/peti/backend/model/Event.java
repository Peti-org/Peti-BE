package com.peti.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "event", schema = "peti", catalog = "peti")
public class Event {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "event_id", nullable = false)
  private UUID eventId;
  @Basic
  @Column(name = "event_time", nullable = false)
  private Timestamp eventTime;
  @Basic
  @Column(name = "event_name", nullable = false, length = 100)
  private String eventName;
  @Basic
  @Column(name = "event_context", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object eventContext;
  @Basic
  @Column(name = "event_is_deleted", nullable = false)
  private boolean eventIsDeleted;
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User userByUserId;
  @ManyToOne
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretakerByCaretakerId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Event event = (Event) o;

    if (eventIsDeleted != event.eventIsDeleted)
      return false;
    if (eventId != null ? !eventId.equals(event.eventId) : event.eventId != null)
      return false;
    if (eventTime != null ? !eventTime.equals(event.eventTime) : event.eventTime != null)
      return false;
    if (eventName != null ? !eventName.equals(event.eventName) : event.eventName != null)
      return false;
    if (eventContext != null ? !eventContext.equals(event.eventContext) : event.eventContext != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = eventId != null ? eventId.hashCode() : 0;
    result = 31 * result + (eventTime != null ? eventTime.hashCode() : 0);
    result = 31 * result + (eventName != null ? eventName.hashCode() : 0);
    result = 31 * result + (eventContext != null ? eventContext.hashCode() : 0);
    result = 31 * result + (eventIsDeleted ? 1 : 0);
    return result;
  }
}
