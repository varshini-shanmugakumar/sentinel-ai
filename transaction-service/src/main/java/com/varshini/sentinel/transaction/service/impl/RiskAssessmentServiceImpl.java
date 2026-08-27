package com.varshini.sentinel.transaction.service.impl;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(100000);
    private static final BigDecimal MEDIUM_VALUE_THRESHOLD = BigDecimal.valueOf(50000);

    private final TransactionRepository transactionRepository;

    @Override
    public RiskAssessment assessTransaction(Transaction transaction){
        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        if(transaction.getAmount() == null){
            throw new IllegalArgumentException("Amount must not be null");
        }

        if(transaction.getAmount().compareTo(HIGH_VALUE_THRESHOLD) >= 0){
            riskScore += 40;
            reasons.add("High value transaction");
        } else if(transaction.getAmount().compareTo(MEDIUM_VALUE_THRESHOLD) >= 0){
            riskScore += 20;
            reasons.add("Medium value transaction");
        }

        Instant cutoffTime = transaction.getTimeStamp().minus(5, ChronoUnit.MINUTES);
        List<Transaction> recentTransactions = transactionRepository.
                findByFromAccountAndTimeStampAfter(transaction.getFromAccount(), cutoffTime);
        long recentTransactionCount = recentTransactions.stream()
                .filter(existingTransaction -> !existingTransaction.getTransactionId()
                        .equals(transaction.getTransactionId()))
                .count();
        if(recentTransactionCount >= 3){
            riskScore += 30;
            reasons.add("Multiple transactions in a short time period");
        }

        RiskLevel riskLevel = calculateRiskLevel(riskScore);

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setRiskLevel(riskLevel);
        riskAssessment.setReasons(reasons);
        riskAssessment.setTransactionId(transaction.getTransactionId());
        riskAssessment.setRiskScore(riskScore);

        return riskAssessment;
    }

    private RiskLevel calculateRiskLevel(int riskScore){
        if(riskScore >= 40){
            return RiskLevel.HIGH;
        } else if(riskScore >= 20){
            return RiskLevel.MEDIUM;
        }
        return RiskLevel.LOW;
    }
}
