package com.bankflow.ledger.business.ports;

import com.bankflow.ledger.core.entities.OutboxEntry;

public interface IOutboxRepository {
    void save(OutboxEntry entry);
}