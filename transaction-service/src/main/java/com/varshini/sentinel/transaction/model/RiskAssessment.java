package com.varshini.sentinel.transaction.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Document(collection = "risk_assessments")
public class RiskAssessment {
    @Id
    private String id;
    @Indexed(unique = true)
    private UUID transactionId;

    private int riskScore;
    private RiskLevel  riskLevel;
    private List<String> reasons;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
