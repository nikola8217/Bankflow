package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.domain.models.AccountSnapshot;

import java.util.Optional;
import java.util.UUID;

public interface AccountDirectory {
    Optional<AccountSnapshot> findAccount(UUID accountId);
}