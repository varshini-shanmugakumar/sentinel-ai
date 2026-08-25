package com.varshini.sentinel.transaction.service;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import com.varshini.sentinel.transaction.model.RiskLevel;
import com.varshini.sentinel.transaction.model.Transaction;
import com.varshini.sentinel.transaction.service.impl.RiskAssessmentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentServiceImplTest {

    @InjectMocks
    RiskAssessmentServiceImpl riskAssessmentService;

    @Test
    void shouldTestHighValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(1050000));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(40, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.MEDIUM, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().contains("High value transaction"));
    }

    @Test
    void shouldTestMediumValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(50000));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(20, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().contains("Medium value transaction"));
    }

    @Test
    void shouldTestLowValueTransaction(){
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(5000));

        RiskAssessment riskAssessment = riskAssessmentService.assessTransaction(transaction);

        assertNotNull(riskAssessment);
        assertEquals(0, riskAssessment.getRiskScore());
        assertEquals(RiskLevel.LOW, riskAssessment.getRiskLevel());
        assertTrue(riskAssessment.getReasons().isEmpty());
    }
}
