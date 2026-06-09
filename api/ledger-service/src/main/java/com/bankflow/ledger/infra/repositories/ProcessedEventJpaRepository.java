package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.ProcessedEventModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventJpaRepository extends JpaRepository<ProcessedEventModel, UUID> {}