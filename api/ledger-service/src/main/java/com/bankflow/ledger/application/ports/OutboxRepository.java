package com.bankflow.ledger.application.ports;

import com.bankflow.ledger.application.outbox.OutboxEntry;

public interface OutboxRepository {
    void save(OutboxEntry entry);
}