package com.bankflow.account.business.ports;

import com.bankflow.account.core.entities.Account;

public interface IAccountEventPublisher {
    void accountCreated(Account account);
}