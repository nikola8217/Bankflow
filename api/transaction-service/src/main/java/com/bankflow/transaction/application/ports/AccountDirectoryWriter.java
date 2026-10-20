package com.bankflow.transaction.application.ports;

import com.bankflow.transaction.domain.models.AccountSnapshot;

public interface AccountDirectoryWriter {
    void addIfAbsent(AccountSnapshot account);
}