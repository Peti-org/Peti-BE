package com.peti.backend.model.domain;

import com.peti.backend.model.internal.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "order_modification", schema = "peti")
public class OrderModification {

  @GeneratedValue(strategy = GenerationType.UUID)
  @Id
  @Column(name = "modification_id", nullable = false)
  private UUID modificationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private OrderStatus status;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "actor_id", nullable = false)
  private UUID actorId;

  @Column(name = "comment", length = 500)
  private String comment;

  public static OrderModification create(Order order, OrderStatus status,
      UUID actorId, String comment) {
    OrderModification mod = new OrderModification();
    mod.setOrder(order);
    mod.setStatus(status);
    mod.setCreatedAt(LocalDateTime.now());
    mod.setActorId(actorId);
    mod.setComment(comment);
    return mod;
  }
}

