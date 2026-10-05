package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.application.outbox.OutboxEntry;

public interface OutboxRepository {
    void save(OutboxEntry entry);
//    List<OutboxEntry> findPending();
//    void markAsProcessed(UUID id);
}