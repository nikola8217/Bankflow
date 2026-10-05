package com.bankflow.transaction.persistence.adapters;

import com.bankflow.transaction.application.ports.IdempotencyRepository;
import com.bankflow.transaction.application.idempotency.IdempotencyRecord;
import com.bankflow.transaction.domain.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.persistence.jpa.IdempotencyJpaEntity;
import com.bankflow.transaction.persistence.jpa.repositories.IdempotencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IdempotencyRepository {

    private static final String UNIQUE_CONSTRAINT = "uk_idempotency_user_key";

    private final IdempotencyJpaRepository jpaRepository;

    @Override
    public Optional<IdempotencyRecord> find(UUID userId, String idempotencyKey) {
        return jpaRepository.findByUserIdAndIdempotencyKey(userId, idempotencyKey)
                .map(m -> new IdempotencyRecord(
                        m.getUserId(), m.getIdempotencyKey(), m.getRequestFingerprint(),
                        m.getTransactionId(), m.getCreatedAt()));
    }

    @Override
    public void save(IdempotencyRecord record) {
        IdempotencyJpaEntity model = new IdempotencyJpaEntity();
        model.setUserId(record.userId());
        model.setIdempotencyKey(record.idempotencyKey());
        model.setRequestFingerprint(record.requestFingerprint());
        model.setTransactionId(record.transactionId());
        model.setCreatedAt(record.createdAt());

        try {
            jpaRepository.saveAndFlush(model);
        } catch (DataIntegrityViolationException e) {
            String reason = e.getMostSpecificCause().getMessage();
            if (reason != null && reason.contains(UNIQUE_CONSTRAINT)) {
                throw new IdempotencyKeyConflictException();
            }
            throw e;
        }
    }
}