package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.exception.SameAccountTransferException;
import com.varshini.sentinel.transaction.exception.TransactionNotFoundException;
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


    @Test
    void shouldCreateTransactionSuccessfully() {
        request.setFromAccount("ACC1001");
        request.setToAccount("ACC2001");
        request.setAmount(new BigDecimal("1000"));
        request.setCurrency("INR");

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
}
