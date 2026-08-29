package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.HighRiskTransactionException;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.exception.TransactionNotFoundException;
import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.repository.TransactionRepository;
import com.varshini.sentinel.transaction.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {
    CreateTransactionRequest request = new CreateTransactionRequest();
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionServiceImpl transactionService;
    @Mock
    private RiskAssessmentService riskAssessmentService;


    @Test
    void shouldCreateTransactionSuccessfully() {
        request.setFromAccount("ACC1001");
        request.setToAccount("ACC2001");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("INR");

        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setRiskLevel(RiskLevel.LOW);

        when(riskAssessmentService.assessTransaction(any()))
                .thenReturn(riskAssessment);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction result = transactionService.createTransaction(request);

        assertNotNull(result);
        assertEquals("ACC1001", result.getFromAccount());
        assertEquals("ACC2001", result.getToAccount());
        assertEquals(new BigDecimal("1000"), result.getAmount());
        assertEquals("INR", result.getCurrency());
        assertEquals(TransactionStatus.PENDING, result.getStatus());
        assertNotNull(result.getTransactionId());
        assertNotNull(result.getTimeStamp());
        verify(transactionRepository).save(any(Transaction.class));
        verify(riskAssessmentService).assessTransaction(result);
    }

    @Test
    void shouldRejectTransferWhenAccountsAreSame() {
        request.setFromAccount("ACC1001");
        request.setToAccount("ACC1001");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("INR");

        assertThrows(
                SameAccountTransferException.class,
                () -> transactionService.createTransaction(request)
        );
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(riskAssessmentService, never()).assessTransaction(any(Transaction.class));
    }

    @Test
    void shouldRejectTransferWhenAccountsDifferOnlyByCase() {
        request.setFromAccount("ACC1001");
        request.setToAccount("acc1001");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("INR");

        assertThrows(
                SameAccountTransferException.class,
                () -> transactionService.createTransaction(request)
        );
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldGetTransactionByIdSuccessfully() {
        UUID transactionId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setFromAccount("ACC1001");
        transaction.setToAccount("ACC2001");
        transaction.setAmount(new BigDecimal("1000"));
        transaction.setCurrency("INR");
        transaction.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(transaction));

        Transaction result = transactionService.getTransactionById(transactionId);

        assertNotNull(result);
        assertEquals(transactionId, result.getTransactionId());
        assertEquals("ACC1001", result.getFromAccount());

        verify(transactionRepository).findByTransactionId(transactionId);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFound() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.getTransactionById(transactionId));

        assertEquals("Transaction not found: "+transactionId, exception.getMessage());
        verify(transactionRepository).findByTransactionId(transactionId);
    }

    @Test
    void shouldGetAllTransactionsSuccessfully() {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setToAccount("ACC2001");

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC1002");
        transaction2.setToAccount("ACC2002");

        List<Transaction> transactions = List.of(transaction1, transaction2);
        when(transactionRepository.findAll()).thenReturn(transactions);

        List<Transaction> result = transactionService.getAllTransactions();
        assertNotNull(result);
        assertEquals(transactions.size(), result.size());
        assertEquals("ACC1001", result.get(0).getFromAccount());
        assertEquals("ACC1002", result.get(1).getFromAccount());

        verify(transactionRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactionsExist() {
        when(transactionRepository.findAll()).thenReturn(List.of());
        List<Transaction> result = transactionService.getAllTransactions();
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).findAll();
    }

    @Test
    void shouldUpdateTransactionStatusSuccessfully() {
        UUID transactionId = UUID.randomUUID();

        Transaction transaction = new Transaction();
        transaction.setTransactionId(transactionId);
        transaction.setFromAccount("ACC1001");
        transaction.setToAccount("ACC2001");
        transaction.setAmount(new BigDecimal("1000"));
        transaction.setCurrency("INR");
        transaction.setStatus(TransactionStatus.PENDING);

        when(transactionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction updatedTransaction = transactionService.updateTransactionStatus(transactionId, TransactionStatus.APPROVED);

        assertEquals(TransactionStatus.APPROVED, updatedTransaction.getStatus());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldThrowExceptionWhenTransactionNotFoundForUpdate() {
        UUID transactionId = UUID.randomUUID();

        when(transactionRepository.findByTransactionId(transactionId))
                .thenReturn(Optional.empty());

        TransactionNotFoundException exception = assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.updateTransactionStatus(transactionId,  TransactionStatus.APPROVED));

        assertEquals("Transaction not found: "+transactionId, exception.getMessage());
        verify(transactionRepository).findByTransactionId(transactionId);
    }

    @Test
    void shouldFilterTransactionsSuccessfully() {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setToAccount("ACC2001");
        transaction1.setAmount(new BigDecimal("1000"));
        transaction1.setCurrency("INR");
        transaction1.setStatus(TransactionStatus.PENDING);

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC1002");
        transaction2.setToAccount("ACC2002");
        transaction2.setAmount(new BigDecimal("1000"));
        transaction2.setCurrency("INR");
        transaction2.setStatus(TransactionStatus.PENDING);

        List<Transaction> transactions = List.of(transaction1, transaction2);

        when(transactionRepository.findByStatus(TransactionStatus.PENDING))
                .thenReturn(transactions);

        List<Transaction> result = transactionService.getTransactionsByStatus(TransactionStatus.PENDING);
        assertNotNull(result);
        assertEquals(transactions, result);
        verify(transactionRepository).findByStatus(TransactionStatus.PENDING);
    }

    @Test
    void shouldThrowExceptionForHighRiskTransaction() {
        RiskAssessment riskAssessment = new RiskAssessment();
        riskAssessment.setRiskLevel(RiskLevel.HIGH);

        when(riskAssessmentService.assessTransaction(any(Transaction.class)))
                .thenReturn(riskAssessment);

        assertThrows(
                HighRiskTransactionException.class,
                () -> transactionService.createTransaction(request)
        );

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldNotSaveTransactionIfRiskEngineFails(){
        when(riskAssessmentService.assessTransaction(any(Transaction.class)))
                .thenThrow(new RuntimeException("Risk engine unavailable"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.createTransaction(request)
        );

        assertEquals("Risk engine unavailable", exception.getMessage());

        verify(transactionRepository, never()).save(any(Transaction.class));
    }

}
