package com.bankflow.transaction.infra.repositories;

import com.bankflow.transaction.core.enums.OutboxStatus;
import com.bankflow.transaction.infra.models.OutboxModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxModel, UUID> {
    List<OutboxModel> findByStatus(OutboxStatus status);
}