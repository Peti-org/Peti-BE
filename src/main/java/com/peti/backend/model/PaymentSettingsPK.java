package com.peti.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

public class PaymentSettingsPK implements Serializable {
  @Column(name = "user_id", nullable = false)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Getter
  @Setter
  private UUID userId;
  @Column(name = "payment_id", nullable = false)
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int paymentId;

  public int getPaymentId() {
    return paymentId;
  }

  public void setPaymentId(int paymentId) {
    this.paymentId = paymentId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    PaymentSettingsPK that = (PaymentSettingsPK) o;

    if (paymentId != that.paymentId)
      return false;
    if (userId != null ? !userId.equals(that.userId) : that.userId != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + paymentId;
    return result;
  }
}
