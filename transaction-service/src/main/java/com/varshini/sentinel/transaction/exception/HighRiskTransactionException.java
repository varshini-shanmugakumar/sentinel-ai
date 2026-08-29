package com.varshini.sentinel.transaction.exception;

public class HighRiskTransactionException extends RuntimeException {
    public HighRiskTransactionException(String message) {
        super(message);
    }
}
