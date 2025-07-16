package com.peti.backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "caretaker", schema = "peti", catalog = "peti")
public class Caretaker {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "caretaker_id", nullable = false)
  private UUID caretakerId;

  @Basic
  @Column(name = "caretaker_preference", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private Object caretakerPreference;

  @Basic
  @Column(name = "rating ", nullable = false)
  private int rating;

  @Basic
  @Column(name = "caretaker_is_deleted", nullable = false)
  private boolean caretakerIsDeleted;
  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User userByUserId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Caretaker caretaker = (Caretaker) o;

    if (caretakerIsDeleted != caretaker.caretakerIsDeleted)
      return false;
    if (caretakerId != null ? !caretakerId.equals(caretaker.caretakerId) : caretaker.caretakerId != null)
      return false;
    if (caretakerPreference != null ? !caretakerPreference.equals(caretaker.caretakerPreference) :
            caretaker.caretakerPreference != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = caretakerId != null ? caretakerId.hashCode() : 0;
    result = 31 * result + (caretakerPreference != null ? caretakerPreference.hashCode() : 0);
    result = 31 * result + (caretakerIsDeleted ? 1 : 0);
    return result;
  }

}
