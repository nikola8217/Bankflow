package com.bankflow.transaction.infra.repositories;

import com.bankflow.transaction.infra.models.IdempotencyModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyModel, UUID> {
    Optional<IdempotencyModel> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}