package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.domain.models.AccountSnapshot;

import java.util.UUID;

public interface AccountClient {
    AccountSnapshot getAccount(UUID accountId);
}
