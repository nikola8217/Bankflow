package com.bankflow.transaction.application.ports;

import com.bankflow.shared.events.TransactionCreatedEvent;

public interface TransactionEventPublisher {
    void transactionCreated(TransactionCreatedEvent event);
}