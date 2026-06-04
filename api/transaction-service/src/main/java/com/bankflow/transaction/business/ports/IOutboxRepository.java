package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.entities.OutboxEntry;
import java.util.List;
import java.util.UUID;

public interface IOutboxRepository {
    void save(OutboxEntry entry);
//    List<OutboxEntry> findPending();
//    void markAsProcessed(UUID id);
}