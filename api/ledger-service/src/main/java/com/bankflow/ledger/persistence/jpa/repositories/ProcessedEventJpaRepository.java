package com.bankflow.ledger.persistence.jpa.repositories;

import com.bankflow.ledger.persistence.jpa.ProcessedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, String> {}