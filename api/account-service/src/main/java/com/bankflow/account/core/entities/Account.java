package com.bankflow.account.core.entities;

import com.bankflow.account.core.enums.AccountStatus;
import com.bankflow.account.core.enums.AccountType;
import com.bankflow.account.core.enums.Currency;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Account {
    private final UUID id;
    private final UUID userId;
    private final AccountType type;
    private AccountStatus status;
    private final Currency currency;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Account(UUID id, UUID userId, AccountType type, AccountStatus status, Currency currency) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.currency = currency;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }
}
