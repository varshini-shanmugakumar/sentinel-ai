package com.varshini.sentinel.transaction.dto;

import com.varshini.sentinel.transaction.model.TransactionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTransactionStatusRequest {
    private TransactionStatus status;
}
