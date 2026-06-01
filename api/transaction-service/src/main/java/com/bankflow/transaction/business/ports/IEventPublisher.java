package com.bankflow.transaction.business.ports;

import com.bankflow.shared.events.TransactionCreatedEvent;

public interface IEventPublisher {
    void publish(TransactionCreatedEvent event);
}