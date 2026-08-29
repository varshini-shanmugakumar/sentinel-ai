package com.varshini.sentinel.transaction.service.impl;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.HighRiskTransactionException;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.exception.TransactionNotFoundException;
import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.RiskAssessmentService;
import com.varshini.sentinel.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final RiskAssessmentService riskAssessmentService;

    public Transaction  createTransaction(CreateTransactionRequest request) throws SameAccountTransferException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimeStamp(Instant.now());
        // building from request
        String from = request.getFromAccount();
        String to = request.getToAccount();
        if (from != null && from.equalsIgnoreCase(to)) {
            throw new SameAccountTransferException("Source and Destination accounts cannot be the same");
        }
        transaction.setFromAccount(request.getFromAccount());
        transaction.setToAccount(request.getToAccount());
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        if(riskAssessment.getRiskLevel() == RiskLevel.HIGH){
            throw new HighRiskTransactionException("Transaction blocked due to high risk");
        }

        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction getTransactionById(UUID transactionId) {
        return transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found: "+transactionId));
    }

    @Override
    public List<Transaction> getAllTransactions(){
        return transactionRepository.findAll();
    }

    @Override
    public Transaction updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(
                        "Transaction not found: "+transactionId
                ));
        transaction.setStatus(status);
        return transactionRepository.save(transaction);
    }

    @Override
    public List<Transaction> getTransactionsByStatus(TransactionStatus status) {
        return transactionRepository.findByStatus(status);
    }
}
