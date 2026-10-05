package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.application.idempotency.IdempotencyRecord;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyRepository {
    Optional<IdempotencyRecord> find(UUID userId, String idempotencyKey);
    void save(IdempotencyRecord record);
}