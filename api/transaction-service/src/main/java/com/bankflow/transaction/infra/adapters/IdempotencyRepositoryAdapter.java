package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.infra.models.IdempotencyModel;
import com.bankflow.transaction.infra.repositories.IdempotencyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class IdempotencyRepositoryAdapter implements IIdempotencyRepository {

    private final IdempotencyJpaRepository jpaRepository;

    @Override
    public boolean exists(String idempotencyKey) {
        return jpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public void save(String idempotencyKey) {
        IdempotencyModel model = new IdempotencyModel();
        model.setIdempotencyKey(idempotencyKey);
        model.setCreatedAt(LocalDateTime.now());
        jpaRepository.save(model);
    }
}