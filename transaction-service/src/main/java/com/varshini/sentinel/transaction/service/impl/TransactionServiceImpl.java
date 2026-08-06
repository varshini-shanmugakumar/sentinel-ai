package com.varshini.sentinel.transaction.service.impl;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.service.TransactionService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionServiceImpl implements TransactionService {
    public Transaction  createTransaction(CreateTransactionRequest request) throws SameAccountTransferException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimeStamp(Instant.now());
        // building from request
        if(Objects.equals(request.getFromAccount(), request.getToAccount())){
            throw new SameAccountTransferException("Source and Destination accounts cannot be the same");
        }
        transaction.setFromAccount(request.getFromAccount());
        transaction.setToAccount(request.getToAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());

        return transaction;
    }
}
