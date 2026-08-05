package com.varshini.sentinel.transaction.controller;

import com.varshini.sentinel.transaction.dto.CreateTransactionRequest;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1")
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> createTransaction(@Valid @RequestBody CreateTransactionRequest request) throws Exception {
        Transaction transaction = transactionService.createTransaction(request);
        return ResponseEntity.ok(transaction);
    }
}
