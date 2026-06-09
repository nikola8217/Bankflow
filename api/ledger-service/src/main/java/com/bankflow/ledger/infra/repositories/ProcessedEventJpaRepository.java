package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.ProcessedEventModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventModel, String> {}