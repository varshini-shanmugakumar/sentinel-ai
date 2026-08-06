package com.varshini.sentinel.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import  java.math.BigDecimal;

@Setter
@Getter
public class CreateTransactionRequest {
    @NotBlank(message = "from account should not be blank")
    private String fromAccount;

    @NotBlank(message = "to account should not be blank")
    private String toAccount;

    @Positive(message = "the amount must be a positive value")
    private BigDecimal amount;

    @NotBlank
    private String currency;

}
