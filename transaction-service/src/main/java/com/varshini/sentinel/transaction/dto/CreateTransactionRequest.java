package com.varshini.sentinel.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class CreateTransactionRequest {
    @NotBlank(message = "from account should not be blank")
    private String fromAccount;

    @NotBlank(message = "to account should not be blank")
    private String toAccount;

    @Positive(message = "the amount must be a positive value")
    private float amount;

    @NotBlank
    private String currency;

    public String getFromAccount() {
        return fromAccount;
    }
    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }
    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public float getAmount() {
        return amount;
    }
    public void setAmount(float amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }
    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
