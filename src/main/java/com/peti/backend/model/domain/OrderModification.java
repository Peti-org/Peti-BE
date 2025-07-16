package com.peti.backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order_modification", schema = "peti", catalog = "peti")
@IdClass(OrderModificationPK.class)
public class OrderModification {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "order_id", nullable = false)
  private UUID orderId;
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "modification_id", nullable = false)
  private int modificationId;
  @Basic
  @Column(name = "type", nullable = false, length = 20)
  private String type;
  @Basic
  @Column(name = "time", nullable = false)
  private Timestamp time;
//  @ManyToOne
//  @JoinColumn(name = "order_id", referencedColumnName = "order_id", nullable = false)
//  private Order orderByOrderId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OrderModification that = (OrderModification) o;

    if (modificationId != that.modificationId)
      return false;
    if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null)
      return false;
    if (type != null ? !type.equals(that.type) : that.type != null)
      return false;
    if (time != null ? !time.equals(that.time) : that.time != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = orderId != null ? orderId.hashCode() : 0;
    result = 31 * result + modificationId;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (time != null ? time.hashCode() : 0);
    return result;
  }
}
