package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.TransactionHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TransactionHistoryJpaRepository extends JpaRepository<TransactionHistoryModel, UUID> {

    @Query("""
            SELECT h FROM TransactionHistoryModel h
            WHERE h.accountId = :accountId
               OR (h.targetAccountId = :accountId
                   AND h.status = com.bankflow.shared.enums.TransactionStatus.COMPLETED)
            ORDER BY h.createdAt DESC
            """)
    List<TransactionHistoryModel> findStatement(@Param("accountId") UUID accountId);
}