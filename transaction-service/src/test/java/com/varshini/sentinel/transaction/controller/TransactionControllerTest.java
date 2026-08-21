package com.varshini.sentinel.transaction.controller;

import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.model.TransactionStatus;
import com.varshini.sentinel.transaction.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;

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
}
