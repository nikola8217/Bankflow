package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.aggregates.TransactionAggregate;

import java.util.Optional;
import java.util.UUID;

public interface IEventStore {
    void save(TransactionAggregate aggregate);
    Optional<TransactionAggregate> load(UUID transactionId);
}