package com.peti.backend.dto;

import java.io.Serializable;

public class PaymentSettingsDto implements Serializable {
    private Long userId;
    private String paymentMethod;
    private String accountDetails;

    public PaymentSettingsDto() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(String accountDetails) {
        this.accountDetails = accountDetails;
    }
}