package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.entities.IdempotencyRecord;

import java.util.Optional;
import java.util.UUID;

public interface IIdempotencyRepository {
    Optional<IdempotencyRecord> find(UUID userId, String idempotencyKey);
    void save(IdempotencyRecord record);
}