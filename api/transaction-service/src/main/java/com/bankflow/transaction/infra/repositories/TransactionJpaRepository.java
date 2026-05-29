package com.bankflow.transaction.infra.repositories;

import com.bankflow.transaction.infra.models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionModel, UUID> {
    List<TransactionModel> findAllByAccountId(UUID accountId);
}