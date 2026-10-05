package com.bankflow.ledger.application.ports;

import com.bankflow.ledger.domain.models.Balance;

import java.util.Optional;
import java.util.UUID;

public interface BalanceRepository {
    void save(Balance balance);
    Optional<Balance> findByAccountId(UUID accountId);
    void ensureExists(UUID accountId, String currency);
    Optional<Balance> findByAccountIdForUpdate(UUID accountId);
    void registerAccount(UUID accountId, UUID userId, String currency);
}