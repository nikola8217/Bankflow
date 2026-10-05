package com.bankflow.ledger.persistence.jpa.repositories;

import com.bankflow.ledger.persistence.jpa.TransactionHistoryJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionHistoryJpaRepository extends JpaRepository<TransactionHistoryJpaEntity, UUID> {

    @Query("""
            SELECT h FROM TransactionHistoryJpaEntity h
            WHERE h.accountId = :accountId
               OR (h.targetAccountId = :accountId
                   AND h.status = com.bankflow.shared.enums.TransactionStatus.COMPLETED)
            ORDER BY h.createdAt DESC
            """)
    List<TransactionHistoryJpaEntity> findStatement(@Param("accountId") UUID accountId);
}