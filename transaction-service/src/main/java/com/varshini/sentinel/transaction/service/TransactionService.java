package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.model.Transaction;

import java.util.List;
import java.util.UUID;


public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request) throws SameAccountTransferException;
    Transaction getTransactionById(UUID transactionId);
    List<Transaction> getAllTransactions();
}
