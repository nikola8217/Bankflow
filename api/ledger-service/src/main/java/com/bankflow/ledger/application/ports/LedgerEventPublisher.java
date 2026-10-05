package com.bankflow.ledger.application.ports;

import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;

public interface LedgerEventPublisher {
    void transactionApproved(TransactionApprovedEvent event);

    void transactionDeclined(TransactionDeclinedEvent event);
}