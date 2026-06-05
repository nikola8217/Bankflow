package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.OutboxModel;
import com.bankflow.shared.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxModel, UUID> {
    List<OutboxModel> findByStatus(OutboxStatus status);
}