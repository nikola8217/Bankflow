package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.TransactionHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionHistoryJpaRepository extends JpaRepository<TransactionHistoryModel, UUID> {
    List<TransactionHistoryModel> findAllByAccountId(UUID accountId);
}