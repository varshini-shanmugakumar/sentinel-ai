package com.varshini.sentinel.transaction.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RiskAssessment {
    private UUID transactionId;
    private int riskScore;
    private RiskLevel  riskLevel;
    private List<String> reasons;
}
