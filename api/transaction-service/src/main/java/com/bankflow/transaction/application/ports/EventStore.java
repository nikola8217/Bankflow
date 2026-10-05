package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.domain.models.TransactionAggregate;

import java.util.Optional;
import java.util.UUID;

public interface EventStore {
    void save(TransactionAggregate aggregate);
    Optional<TransactionAggregate> load(UUID transactionId);
}