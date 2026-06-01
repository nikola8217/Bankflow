package com.bankflow.balance.business.ports;

import com.bankflow.balance.core.entities.Balance;

import java.util.Optional;
import java.util.UUID;

public interface IBalanceRepository {
    Balance create(Balance balance);
    Balance update(Balance balance);
    Optional<Balance> findByAccountId(UUID accountId);
}