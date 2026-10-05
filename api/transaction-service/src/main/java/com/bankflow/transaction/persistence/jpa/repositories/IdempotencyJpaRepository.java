package com.bankflow.transaction.persistence.jpa.repositories;

import com.bankflow.transaction.persistence.jpa.IdempotencyJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IdempotencyJpaRepository extends JpaRepository<IdempotencyJpaEntity, UUID> {
    Optional<IdempotencyJpaEntity> findByUserIdAndIdempotencyKey(UUID userId, String idempotencyKey);
}