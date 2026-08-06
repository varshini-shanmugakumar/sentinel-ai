package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.model.Transaction;


public interface TransactionService {
    Transaction createTransaction(CreateTransactionRequest request) throws SameAccountTransferException;
}
