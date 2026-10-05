package com.bankflow.account.application.ports;

import com.bankflow.account.domain.models.Account;

public interface AccountEventPublisher {
    void accountCreated(Account account);
}