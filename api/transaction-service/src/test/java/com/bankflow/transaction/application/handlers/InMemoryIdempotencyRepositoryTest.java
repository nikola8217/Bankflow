package com.bankflow.transaction.application.handlers;

import com.bankflow.transaction.application.ports.IdempotencyRepository;
import com.bankflow.transaction.application.idempotency.IdempotencyRecord;
import com.bankflow.transaction.domain.exceptions.IdempotencyKeyConflictException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class InMemoryIdempotencyRepositoryTest implements IdempotencyRepository {

    final Map<String, IdempotencyRecord> records = new HashMap<>();

    @Override
    public Optional<IdempotencyRecord> find(UUID userId, String idempotencyKey) {
        return Optional.ofNullable(records.get(id(userId, idempotencyKey)));
    }

    @Override
    public void save(IdempotencyRecord record) {
        String id = id(record.userId(), record.idempotencyKey());
        if (records.containsKey(id)) {
            throw new IdempotencyKeyConflictException();   // kao UNIQUE constraint u bazi
        }
        records.put(id, record);
    }

    private static String id(UUID userId, String key) {
        return userId + "|" + key;
    }
}