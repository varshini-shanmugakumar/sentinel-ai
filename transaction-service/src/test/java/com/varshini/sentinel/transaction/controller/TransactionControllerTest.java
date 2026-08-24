package com.varshini.sentinel.transaction.controller;

import com.varshini.sentinel.transaction.dto.UpdateTransactionStatusRequest;
import com.varshini.sentinel.transaction.exception.TransactionNotFoundException;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldReturnAllTransactions() throws Exception {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setToAccount("ACC1002");
        transaction1.setAmount(new BigDecimal("10000"));
        transaction1.setCurrency("INR");
        transaction1.setStatus(TransactionStatus.PENDING);
        transaction1.setTimeStamp(Instant.now());

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC2001");
        transaction2.setToAccount("ACC2002");
        transaction2.setAmount(new BigDecimal("20000"));
        transaction2.setCurrency("INR");
        transaction2.setStatus(TransactionStatus.PENDING);
        transaction2.setTimeStamp(Instant.now());

        when(transactionService.getAllTransactions())
                .thenReturn(List.of(transaction1, transaction2));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fromAccount").value("ACC1001"))
                .andExpect(jsonPath("$[0].toAccount").value("ACC1002"));

        verify(transactionService).getAllTransactions();
    }

    @Test
    void shouldReturnEmptyListWhenNoTransactions() throws Exception {
        when(transactionService.getAllTransactions()).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(transactionService).getAllTransactions();
    }

    @Test
    void shouldUpdateStatusSuccessfully() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(UUID.randomUUID());
        transaction.setFromAccount("ACC1001");
        transaction.setToAccount("ACC1002");
        transaction.setAmount(new BigDecimal("10000"));
        transaction.setCurrency("INR");
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setTimeStamp(Instant.now());

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest();
        request.setStatus(TransactionStatus.APPROVED);

        when(transactionService.updateTransactionStatus(
                transaction.getTransactionId(),
                TransactionStatus.APPROVED
        )).thenReturn(transaction);

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/status",
                                transaction.getTransactionId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(transactionService).updateTransactionStatus(transaction.getTransactionId(), TransactionStatus.APPROVED);
    }

    @Test
    void shouldThrowExceptionWhenNoTransactionToUpdate() throws Exception {
        UUID transactionId = UUID.randomUUID();

        UpdateTransactionStatusRequest request = new UpdateTransactionStatusRequest();
        request.setStatus(TransactionStatus.APPROVED);

        when(transactionService.updateTransactionStatus(
                transactionId,
                TransactionStatus.APPROVED
        )).thenThrow(new TransactionNotFoundException(
                "Transaction not found: " + transactionId
        ));

        mockMvc.perform(patch("/api/v1/transactions/{transactionId}/status",
                transactionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isNotFound());
        verify(transactionService).updateTransactionStatus(
                transactionId,
                TransactionStatus.APPROVED
        );
    }

    @Test
    void shouldFilterTransactionsSuccessfully() throws Exception {
        Transaction transaction1 = new Transaction();
        transaction1.setTransactionId(UUID.randomUUID());
        transaction1.setFromAccount("ACC1001");
        transaction1.setToAccount("ACC1002");
        transaction1.setAmount(new BigDecimal("10000"));
        transaction1.setCurrency("INR");
        transaction1.setStatus(TransactionStatus.PENDING);
        transaction1.setTimeStamp(Instant.now());

        Transaction transaction2 = new Transaction();
        transaction2.setTransactionId(UUID.randomUUID());
        transaction2.setFromAccount("ACC2001");
        transaction2.setToAccount("ACC2002");
        transaction2.setAmount(new BigDecimal("20000"));
        transaction2.setCurrency("INR");
        transaction2.setStatus(TransactionStatus.PENDING);
        transaction2.setTimeStamp(Instant.now());

        List<Transaction> transactions = List.of(transaction1, transaction2);

        when(transactionService.getTransactionsByStatus(TransactionStatus.PENDING))
                .thenReturn(transactions);

        mockMvc.perform(get("/api/v1/transactions")
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("PENDING"));

        verify(transactionService).getTransactionsByStatus(TransactionStatus.PENDING);
    }
}
