package com.bankflow.transaction.persistence.jpa.repositories;

import com.bankflow.transaction.persistence.jpa.TransactionEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionEventJpaRepository extends JpaRepository<TransactionEventJpaEntity, UUID> {
    List<TransactionEventJpaEntity> findByAggregateIdOrderByVersionAsc(UUID aggregateId);
    int countByAggregateId(UUID aggregateId);
}