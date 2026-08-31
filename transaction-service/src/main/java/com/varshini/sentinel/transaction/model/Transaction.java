package com.varshini.sentinel.transaction.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

import java.time.Instant;

@Data
@NoArgsConstructor
@Document(collection = "transactions")
public class Transaction {
    @Id
    private String id; // MongoDB identifier
    private UUID transactionId; // Business identifier
    @Indexed
    private String fromAccount; // index on fromAccount in Mongo so queries remain performant for accounts with many transactions
    private String toAccount;
    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;
    private Instant timeStamp;
}
