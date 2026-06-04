package com.bankflow.transaction.infra.repositories;

import com.bankflow.transaction.infra.models.TransactionSnapshotModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TransactionSnapshotJpaRepository extends JpaRepository<TransactionSnapshotModel, UUID> {
    Optional<TransactionSnapshotModel> findByAggregateId(UUID aggregateId);
}