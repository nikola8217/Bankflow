package com.bankflow.transaction.persistence.jpa.repositories;

import com.bankflow.transaction.persistence.jpa.AccountReplicaJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AccountReplicaJpaRepository extends JpaRepository<AccountReplicaJpaEntity, UUID> {

    @Modifying
    @Query(value = """
            INSERT INTO account_replica (id, user_id, currency, status)
            VALUES (:id, :userId, :currency, :status)
            ON CONFLICT (id) DO NOTHING
            """, nativeQuery = true)
    void insertIfAbsent(@Param("id") UUID id,
                        @Param("userId") UUID userId,
                        @Param("currency") String currency,
                        @Param("status") String status);
}