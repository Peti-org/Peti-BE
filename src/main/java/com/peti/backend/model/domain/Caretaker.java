package com.peti.backend.model.domain;

import com.peti.backend.dto.caretaker.CaretakerPreferences;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "caretaker", schema = "peti", catalog = "peti")
@EqualsAndHashCode
public class Caretaker {
  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "caretaker_id", nullable = false)
  private UUID caretakerId;

  @Basic
  @Column(name = "caretaker_preference", nullable = false)
  @JdbcTypeCode(SqlTypes.JSON)
  private CaretakerPreferences caretakerPreference;

  @Basic
  @Column(name = "rating", nullable = false)
  private int rating;

  @Basic
  @Column(name = "caretaker_is_deleted", nullable = false)
  private boolean caretakerIsDeleted;

  @Basic
  @Column(name = "generated_to")
  private LocalDate generatedTo;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User userReference;

}
