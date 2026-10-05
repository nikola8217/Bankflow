package com.bankflow.ledger.application.ports;

import com.bankflow.ledger.domain.models.TransactionHistory;

import java.util.List;
import java.util.UUID;

public interface TransactionHistoryRepository {
    void save(TransactionHistory history);
    List<TransactionHistory> findAllByAccountId(UUID accountId);
}