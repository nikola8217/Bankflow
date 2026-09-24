package com.bankflow.account.infra.repositories;

import com.bankflow.account.infra.models.OutboxModel;
import com.bankflow.shared.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxModel, UUID> {
    List<OutboxModel> findByStatusOrderByCreatedAtAsc(OutboxStatus status);
}