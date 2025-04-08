package com.peti.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
public class OrderModificationPK implements Serializable {
  @Column(name = "order_id", nullable = false)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private UUID orderId;
  @Column(name = "modification_id", nullable = false)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int modificationId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OrderModificationPK that = (OrderModificationPK) o;

    if (modificationId != that.modificationId)
      return false;
    if (orderId != null ? !orderId.equals(that.orderId) : that.orderId != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = orderId != null ? orderId.hashCode() : 0;
    result = 31 * result + modificationId;
    return result;
  }
}
