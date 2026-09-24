package com.bankflow.ledger.business.ports;

import com.bankflow.ledger.core.entities.Balance;

import java.util.Optional;
import java.util.UUID;

public interface IBalanceRepository {
    void save(Balance balance);
    Optional<Balance> findByAccountId(UUID accountId);
    void ensureExists(UUID accountId, String currency);
    Optional<Balance> findByAccountIdForUpdate(UUID accountId);
}