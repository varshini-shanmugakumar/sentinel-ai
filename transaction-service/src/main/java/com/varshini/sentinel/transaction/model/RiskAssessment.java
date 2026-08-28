package com.varshini.sentinel.transaction.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "risk_assessments")
public class RiskAssessment {
    @Id
    private String id;

    private UUID transactionId;
    private int riskScore;
    private RiskLevel  riskLevel;
    private List<String> reasons;
}
