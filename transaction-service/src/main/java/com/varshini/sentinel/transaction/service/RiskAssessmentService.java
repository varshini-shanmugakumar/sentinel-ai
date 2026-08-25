package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.Transaction;

public interface RiskAssessmentService {
    RiskAssessment assessTransaction(Transaction transaction);
}
