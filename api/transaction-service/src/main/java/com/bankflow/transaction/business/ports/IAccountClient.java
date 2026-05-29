package com.bankflow.transaction.business.ports;

import com.bankflow.transaction.core.valueObjects.AccountSnapshot;

import java.util.UUID;

public interface IAccountClient {
    AccountSnapshot getAccount(UUID accountId, String token);
}
