package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.BalanceModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface BalanceJpaRepository extends JpaRepository<BalanceModel, UUID> {

    Optional<BalanceModel> findByAccountId(UUID accountId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM BalanceModel b WHERE b.accountId = :accountId")
    Optional<BalanceModel> findByAccountIdForUpdate(@Param("accountId") UUID accountId);

    @Modifying
    @Query(value = """
            INSERT INTO balances (id, account_id, amount, currency, updated_at)
            VALUES (gen_random_uuid(), :accountId, 0, :currency, now())
            ON CONFLICT (account_id) DO NOTHING
            """, nativeQuery = true)
    void insertIfMissing(@Param("accountId") UUID accountId, @Param("currency") String currency);

    @Modifying
    @Query(value = """
            INSERT INTO balances (id, user_id, account_id, amount, currency, updated_at)
            VALUES (gen_random_uuid(), :userId, :accountId, 0, :currency, now())
            ON CONFLICT (account_id) DO UPDATE SET user_id = EXCLUDED.user_id
            """, nativeQuery = true)
    void upsertAccount(@Param("accountId") UUID accountId,
                       @Param("userId") UUID userId,
                       @Param("currency") String currency);
}