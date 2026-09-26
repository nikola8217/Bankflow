package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.core.entities.IdempotencyRecord;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.infra.models.IdempotencyModel;
import com.bankflow.transaction.infra.repositories.IdempotencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IIdempotencyRepository {

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
        IdempotencyModel model = new IdempotencyModel();
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