package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.entities.Transaction;

import java.util.List;
import java.util.UUID;

public interface ITransactionRepository {
    Transaction save(Transaction transaction);
    List<Transaction> findAllByAccountId(UUID accountId);
}
