package com.bankflow.ledger.business.ports;

import com.bankflow.ledger.core.entities.TransactionHistory;

import java.util.List;
import java.util.UUID;

public interface ITransactionHistoryRepository {
    void save(TransactionHistory history);
    List<TransactionHistory> findAllByAccountId(UUID accountId);
}