package com.varshini.sentinel.transaction.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import java.time.Instant;

@Data
@NoArgsConstructor
public class Transaction {
    private UUID transactionId;
    private String fromAccount;
    private String toAccount;
    private float amount;
    private String currency;
    private TransactionStatus status;
    private Instant timeStamp;
}
