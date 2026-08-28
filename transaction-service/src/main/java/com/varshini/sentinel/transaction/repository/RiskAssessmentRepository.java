package com.varshini.sentinel.transaction.repository;

import com.varshini.sentinel.transaction.model.RiskAssessment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RiskAssessmentRepository extends MongoRepository<RiskAssessment, String> {

}
