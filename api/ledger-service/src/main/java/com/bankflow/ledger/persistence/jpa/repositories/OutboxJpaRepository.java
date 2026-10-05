package com.bankflow.ledger.persistence.jpa.repositories;

import com.bankflow.ledger.persistence.jpa.OutboxJpaEntity;
import com.bankflow.shared.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxJpaRepository extends JpaRepository<OutboxJpaEntity, UUID> {

    @Query(value = """
            SELECT * FROM outbox
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
            """, nativeQuery = true)
    List<OutboxJpaEntity> lockNextBatch(@Param("limit") int limit);

    @Modifying
    @Query("DELETE FROM OutboxJpaEntity o WHERE o.status = :status AND o.processedAt < :before")
    int deleteByStatusAndProcessedAtBefore(@Param("status") OutboxStatus status,
                                           @Param("before") LocalDateTime before);
}