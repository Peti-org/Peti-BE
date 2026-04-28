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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "reaction", schema = "peti", catalog = "peti")
public class Reaction {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @EqualsAndHashCode.Include
  @Column(name = "reaction_id", nullable = false)
  private UUID reactionId;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
  private User user;

  @Basic
  @Column(name = "target_type", nullable = false, length = 20)
  private String targetType;

  @Basic
  @Column(name = "target_id", nullable = false)
  private UUID targetId;

  @Basic
  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;
}

