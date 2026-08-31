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
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceImplTest {

    @InjectMocks
    RiskAssessmentServiceImpl riskAssessmentService;

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    RiskAssessmentRepository riskAssessmentRepository;

    @Test
    void shouldTestHighValueTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setFromAccount("ACC1001");
        transaction.setAmount(BigDecimal.valueOf(100000));
        transaction.setTimeStamp(Instant.now());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(40, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.HIGH, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons()
                .contains("High value transaction"));
    }

    @Test
    void shouldTestMediumValueTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setFromAccount("ACC1001");
        transaction.setAmount(BigDecimal.valueOf(50000));
        transaction.setTimeStamp(Instant.now());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(20, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons()
                .contains("Medium value transaction"));
    }

    @Test
    void shouldTestLowValueTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setFromAccount("ACC1001");
        transaction.setAmount(BigDecimal.valueOf(49999));
        transaction.setTimeStamp(Instant.now());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        Transaction transaction = new Transaction();

        Exception exception = assertThrows(
                IllegalArgumentException.class,
                () -> riskAssessmentService.assessTransaction(transaction)
        );

        assertEquals("Amount must not be null", exception.getMessage());
    }

    @Test
    void shouldTestRapidTransactions() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        Transaction transaction1 = createTransaction(
                10000,
                currentTime.minus(1, ChronoUnit.MINUTES)
        );

        Transaction transaction2 = createTransaction(
                30000,
                currentTime.minus(2, ChronoUnit.MINUTES)
        );

        Transaction transaction3 = createTransaction(
                40000,
                currentTime.minus(3, ChronoUnit.MINUTES)
        );

        Transaction currentTransaction = createTransaction(
                10000,
                currentTime
        );

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of(
                transaction1,
                transaction2,
                transaction3
        ));

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(30, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons()
                .contains("Multiple transactions in a short time period"));
    }

    @Test
    void shouldTestNoRapidTransactions() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        Transaction currentTransaction = createTransaction(
                10000,
                currentTime
        );

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldSaveRiskAssessment() {
        Transaction transaction = createTransaction(
                10000,
                Instant.parse("2026-08-31T10:00:00Z")
        );
        transaction.setToAccount("ACC1002");

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                anyString(),
                any(Instant.class)
        )).thenReturn(List.of());

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment result =
                riskAssessmentService.assessTransaction(transaction);

        verify(riskAssessmentRepository).save(any(RiskAssessment.class));
        assertNotNull(result);
    }

    @Test
    void shouldTestUnusuallyHighAmount() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        List<Transaction> historicalTransactions =
                createHistoricTransactions(currentTime);

        Transaction currentTransaction =
                createTransaction(25000, currentTime);

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(historicalTransactions);

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(25, riskAssessment.getRiskScore());
        assertTrue(riskAssessment.getReasons()
                .contains("Transaction amount is unusually high"));
    }

    @Test
    void shouldNotFlagTransactionWhenAmountIsWithinHistoricalRange() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        List<Transaction> historicalTransactions =
                createHistoricTransactions(currentTime);

        Transaction currentTransaction =
                createTransaction(15000, currentTime);

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(historicalTransactions);

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldTestInsufficientHistory() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        Transaction transaction1 =
                createTransaction(
                        5000,
                        currentTime.minus(1, ChronoUnit.DAYS)
                );

        Transaction transaction2 =
                createTransaction(
                        6000,
                        currentTime.minus(2, ChronoUnit.DAYS)
                );

        Transaction currentTransaction =
                createTransaction(15000, currentTime);

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(List.of(transaction1, transaction2));

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment riskAssessment =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }

    @Test
    void shouldIgnoreHistoricalTransactionsWithNullAmount() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        Transaction transaction1 =
                createTransaction(5000, currentTime.minus(1, ChronoUnit.DAYS));

        Transaction transaction2 =
                createTransaction(null, currentTime.minus(2, ChronoUnit.DAYS));

        Transaction transaction3 =
                createTransaction(5000, currentTime.minus(3, ChronoUnit.DAYS));

        Transaction transaction4 =
                createTransaction(5000, currentTime.minus(4, ChronoUnit.DAYS));

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(List.of(
                transaction1,
                transaction2,
                transaction3,
                transaction4
        ));

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction currentTransaction =
                createTransaction(10000, currentTime);

        RiskAssessment result =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(result);
        assertEquals(0, result.getRiskScore());
        assertEquals(RiskLevel.LOW, result.getRiskLevel());
        assertTrue(result.getReasons().isEmpty());
    }

    @Test
    void shouldFlagTransactionWhenAmountExceedsThreeTimesHistoricalAverage() {
        Instant currentTime = Instant.parse("2026-08-31T10:00:00Z");

        Transaction transaction1 =
                createTransaction(
                        5000,
                        currentTime.minus(1, ChronoUnit.DAYS)
                );

        Transaction transaction2 =
                createTransaction(
                        6000,
                        currentTime.minus(2, ChronoUnit.DAYS)
                );

        Transaction transaction3 =
                createTransaction(
                        6001,
                        currentTime.minus(3, ChronoUnit.DAYS)
                );

        Transaction currentTransaction =
                createTransaction(18000, currentTime);

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(5, ChronoUnit.MINUTES)
        )).thenReturn(List.of());

        when(transactionRepository.findByFromAccountAndTimeStampAfter(
                "ACC1001",
                currentTime.minus(90, ChronoUnit.DAYS)
        )).thenReturn(List.of(
                transaction1,
                transaction2,
                transaction3
        ));

        when(riskAssessmentRepository.save(any(RiskAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RiskAssessment result =
                riskAssessmentService.assessTransaction(currentTransaction);

        assertNotNull(result);
        assertEquals(25, result.getRiskScore());
        assertTrue(result.getReasons()
                .contains("Transaction amount is unusually high"));
    }

    private Transaction createTransaction(
            Integer amount,
            Instant timeStamp
    ) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setFromAccount("ACC1001");
        transaction.setTimeStamp(timeStamp);

        if (amount != null) {
            transaction.setAmount(BigDecimal.valueOf(amount));
        }

        return transaction;
    }

    private List<Transaction> createHistoricTransactions(
            Instant currentTime
    ) {
        Transaction transaction1 =
                createTransaction(
                        5000,
                        currentTime.minus(1, ChronoUnit.DAYS)
                );

        Transaction transaction2 =
                createTransaction(
                        10000,
                        currentTime.minus(2, ChronoUnit.DAYS)
                );

        Transaction transaction3 =
                createTransaction(
                        5000,
                        currentTime.minus(3, ChronoUnit.DAYS)
                );

        return List.of(
                transaction1,
                transaction2,
                transaction3
        );
    }
}