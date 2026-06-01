package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.events.TransactionCreatedEvent;

public interface IEventPublisher {
    void publish(TransactionCreatedEvent event);
}