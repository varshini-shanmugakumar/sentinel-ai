package com.varshini.sentinel.transaction.service.impl;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;

    public Transaction  createTransaction(CreateTransactionRequest request) throws SameAccountTransferException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimeStamp(Instant.now());
        // building from request
        if(request.getFromAccount().equalsIgnoreCase(request.getToAccount())){
            throw new SameAccountTransferException("Source and Destination accounts cannot be the same");
        }
        transaction.setFromAccount(request.getFromAccount());
        transaction.setToAccount(request.getToAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());

        return transactionRepository.save(transaction);
    }
}
