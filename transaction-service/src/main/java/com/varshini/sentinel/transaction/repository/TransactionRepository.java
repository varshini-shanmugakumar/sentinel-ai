package com.varshini.sentinel.transaction.repository;

import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    Optional<Transaction> findByTransactionId(UUID transactionId);
    List<Transaction> findByStatus(TransactionStatus status);
}
