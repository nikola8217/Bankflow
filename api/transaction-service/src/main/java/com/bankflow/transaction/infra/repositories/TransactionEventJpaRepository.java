package com.bankflow.transaction.infra.repositories;

import com.bankflow.transaction.infra.models.TransactionEventModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionEventJpaRepository extends JpaRepository<TransactionEventModel, UUID> {
    List<TransactionEventModel> findByAggregateIdOrderByVersionAsc(UUID aggregateId);
    int countByAggregateId(UUID aggregateId);
}