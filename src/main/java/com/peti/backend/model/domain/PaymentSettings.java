package com.peti.backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payment_settings", schema = "peti", catalog = "peti")
@IdClass(PaymentSettingsPK.class)
@Getter
@Setter
public class PaymentSettings {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "user_id", nullable = false)
  private UUID userId;
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  @Column(name = "payment_id", nullable = false)
  private int paymentId;
  @Basic
  @Column(name = "card_number", nullable = false, length = 20)
  private String cardNumber;
  @Basic
  @Column(name = "cvv", nullable = false, length = 4)
  private String cvv;
  @Basic
  @Column(name = "pin", nullable = false, length = 4)
  private String pin;
//  @ManyToOne
//  @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false)
//  private User userByUserId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    PaymentSettings that = (PaymentSettings) o;

    if (paymentId != that.paymentId)
      return false;
    if (userId != null ? !userId.equals(that.userId) : that.userId != null)
      return false;
    if (cardNumber != null ? !cardNumber.equals(that.cardNumber) : that.cardNumber != null)
      return false;
    if (cvv != null ? !cvv.equals(that.cvv) : that.cvv != null)
      return false;
    if (pin != null ? !pin.equals(that.pin) : that.pin != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = userId != null ? userId.hashCode() : 0;
    result = 31 * result + paymentId;
    result = 31 * result + (cardNumber != null ? cardNumber.hashCode() : 0);
    result = 31 * result + (cvv != null ? cvv.hashCode() : 0);
    result = 31 * result + (pin != null ? pin.hashCode() : 0);
    return result;
  }
}
