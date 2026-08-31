package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.repository.RiskAssessmentRepository;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.impl.RiskAssessmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceImplTest {

    @InjectMocks
    RiskAssessmentServiceImpl riskAssessmentService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    RiskAssessmentRepository riskAssessmentRepository;

    @Test
    void shouldTestHighValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100000));
        transaction.setTimeStamp(Instant.now());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(40, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.HIGH, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().contains("High value transaction"));
    }

    @Test
    void shouldTestMediumValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setTimeStamp(Instant.now());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(20, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().contains("Medium value transaction"));
    }

    @Test
    void shouldTestLowValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(49999));
        transaction.setTimeStamp(Instant.now());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull(){
        Transaction transaction = new Transaction();

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            riskAssessmentService.assessTransaction(transaction);});

        assertEquals("Amount must not be null", exception.getMessage());

    }

    @Test
    void shouldTestRapidTransactions(){
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setAmount(BigDecimal.valueOf(10000));
        transaction1.setTimeStamp(Instant.now());

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC1001");
        transaction2.setAmount(BigDecimal.valueOf(30000));
        transaction2.setTimeStamp(Instant.now());

        Transaction transaction3 = new Transaction();
        transaction3.setTransactionId(UUID.randomUUID());
        transaction3.setFromAccount("ACC1001");
        transaction3.setAmount(BigDecimal.valueOf(50000));
        transaction3.setTimeStamp(Instant.now());

        Transaction currentTransaction = new Transaction();
        currentTransaction.setTransactionId(UUID.randomUUID());
        currentTransaction.setFromAccount("ACC1001");
        currentTransaction.setAmount(BigDecimal.valueOf(10000));
        currentTransaction.setTimeStamp(Instant.now());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(eq("ACC1001"), any(Instant.class)))
                .thenReturn(List.of(transaction1, transaction2, transaction3));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(30, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().contains("Multiple transactions in a short time period"));
    }

    @Test
    void shouldTestNoRapidTransactions(){
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setAmount(BigDecimal.valueOf(10000));
        transaction1.setTimeStamp(Instant.now());

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC1001");
        transaction2.setAmount(BigDecimal.valueOf(30000));
        transaction2.setTimeStamp(Instant.now());

        Transaction currentTransaction = new Transaction();
        currentTransaction.setTransactionId(UUID.randomUUID());
        currentTransaction.setFromAccount("ACC1001");
        currentTransaction.setAmount(BigDecimal.valueOf(10000));
        currentTransaction.setTimeStamp(Instant.now());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(eq("ACC1001"), any(Instant.class)))
                .thenReturn(List.of(transaction1, transaction2));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldSaveRiskAssessment(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(10000));
        transaction.setTimeStamp(Instant.now());
        transaction.setFromAccount("ACC1001");
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setToAccount("ACC1002");

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(List.of());
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment result = riskAssessmentService.assessTransaction(transaction);

        verify(riskAssessmentRepository).save(any(RiskAssessment.class));
        assertNotNull(result);
    }

    private void createHistoricTransactions(){
        Transaction transaction1 = new Transaction();
        transaction1.setAmount(BigDecimal.valueOf(5000));
        transaction1.setFromAccount("ACC1001");

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(BigDecimal.valueOf(10000));
        transaction2.setFromAccount("ACC1001");

        Transaction transaction3 = new Transaction();
        transaction3.setAmount(BigDecimal.valueOf(5000));
        transaction3.setFromAccount("ACC1001");

        when(transactionRepository.findByFromAccount("ACC1001"))
                .thenReturn(List.of(transaction1, transaction2, transaction3));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldTestUnusuallyHighAmount(){
        createHistoricTransactions();

        Transaction currentTransaction = new Transaction();
        currentTransaction.setFromAccount("ACC1001");
        currentTransaction.setAmount(BigDecimal.valueOf(25000));
        currentTransaction.setTimeStamp(Instant.now());

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(25, riskAssessment.getRiskScore());
        assertTrue(riskAssessment.getReasons().contains("Transaction amount is unusually high"));
    }

    @Test
    void shouldNotFlagTransactionWhenAmountIsWithinHistoricalRange(){
        createHistoricTransactions();

        Transaction currentTransaction = new Transaction();
        currentTransaction.setFromAccount("ACC1001");
        currentTransaction.setAmount(BigDecimal.valueOf(15000));
        currentTransaction.setTimeStamp(Instant.now());

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldTestInsufficientHistory() {
        Transaction transaction1 = new Transaction();
        transaction1.setAmount(BigDecimal.valueOf(5000));
        transaction1.setFromAccount("ACC1001");

        Transaction transaction2 = new Transaction();
        transaction2.setAmount(BigDecimal.valueOf(10000));
        transaction2.setFromAccount("ACC1001");

        when(transactionRepository.findByFromAccount("ACC1001"))
                .thenReturn(List.of(transaction1, transaction2));
        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction currentTransaction = new Transaction();
        currentTransaction.setFromAccount("ACC1001");
        currentTransaction.setAmount(BigDecimal.valueOf(15000));
        currentTransaction.setTimeStamp(Instant.now());

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }
}
