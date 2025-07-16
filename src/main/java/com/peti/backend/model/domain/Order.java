package com.peti.backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order", schema = "peti", catalog = "peti")
public class Order {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "order_id", nullable = false)
  private UUID orderId;
  @Basic
  @Column(name = "creation_time", nullable = false)
  private Timestamp creationTime;
  @Basic
  @Column(name = "price", nullable = false)
  private BigDecimal price;
  @Basic
  @Column(name = "currency", nullable = false, length = 5)
  private String currency;
  @Basic
  @Column(name = "order_is_deleted", nullable = false, columnDefinition = "numeric", precision = 20, scale = 2)
  private boolean orderIsDeleted;
  @ManyToOne
  @JoinColumn(name = "client_id", referencedColumnName = "user_id", nullable = false)
  private User userByClientId;
  @ManyToOne
  @JoinColumn(name = "caretaker_id", referencedColumnName = "caretaker_id", nullable = false)
  private Caretaker caretakerByCaretakerId;
  @ManyToOne
  @JoinColumn(name = "event_id", referencedColumnName = "event_id", nullable = false)
  private Event eventByEventId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    Order order = (Order) o;

    if (orderIsDeleted != order.orderIsDeleted)
      return false;
    if (orderId != null ? !orderId.equals(order.orderId) : order.orderId != null)
      return false;
    if (creationTime != null ? !creationTime.equals(order.creationTime) : order.creationTime != null)
      return false;
    if (price != null ? !price.equals(order.price) : order.price != null)
      return false;
    if (currency != null ? !currency.equals(order.currency) : order.currency != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = orderId != null ? orderId.hashCode() : 0;
    result = 31 * result + (creationTime != null ? creationTime.hashCode() : 0);
    result = 31 * result + (price != null ? price.hashCode() : 0);
    result = 31 * result + (currency != null ? currency.hashCode() : 0);
    result = 31 * result + (orderIsDeleted ? 1 : 0);
    return result;
  }

}
