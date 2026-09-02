package com.varshini.sentinel.transaction.service.impl;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.repository.RiskAssessmentRepository;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(100000);
    private static final BigDecimal MEDIUM_VALUE_THRESHOLD = BigDecimal.valueOf(50000);
    private static final int MIN_HISTORICAL_TXN_COUNT = 3;
    private static final int HISTORICAL_WINDOW_DAYS = 90;
    private static final int HIGH_AMOUNT_MULTIPLIER = 3;

    private final TransactionRepository transactionRepository;
    private final RiskAssessmentRepository riskAssessmentRepository;

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
                .filter(existingTransaction ->
                        !Objects.equals(
                                existingTransaction.getTransactionId(),
                                transaction.getTransactionId()
                        ))
                .count();
        if(recentTransactionCount >= 3){
            riskScore += 30;
            reasons.add("Multiple transactions in a short time period");
        }

        Instant historicalCutoff = transaction.getTimeStamp().minus(HISTORICAL_WINDOW_DAYS, ChronoUnit.DAYS);

        List<Transaction> historicalTransactions =
                transactionRepository.findByFromAccountAndTimeStampAfter(transaction.getFromAccount(), historicalCutoff);

        List<BigDecimal> historicalAmounts = historicalTransactions.stream()
                .map(Transaction::getAmount)
                .filter(Objects::nonNull)
                .toList();

        if (historicalAmounts.size() >= MIN_HISTORICAL_TXN_COUNT) {

            BigDecimal averageAmount = historicalAmounts.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(
                            BigDecimal.valueOf(historicalAmounts.size()),
                            2,
                            RoundingMode.HALF_UP
                    );

            BigDecimal threshold = averageAmount
                    .multiply(BigDecimal.valueOf(HIGH_AMOUNT_MULTIPLIER));

            if (transaction.getAmount().compareTo(threshold) > 0) {
                riskScore += 25;
                reasons.add("Transaction amount is unusually high");
            }
        }

        RiskLevel riskLevel = calculateRiskLevel(riskScore);

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setRiskLevel(riskLevel);
        riskAssessment.setReasons(reasons);
        riskAssessment.setTransactionId(transaction.getTransactionId());
        riskAssessment.setRiskScore(riskScore);

        return riskAssessmentRepository.save(riskAssessment);
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
