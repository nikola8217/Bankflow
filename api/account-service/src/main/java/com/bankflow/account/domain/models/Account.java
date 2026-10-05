package com.bankflow.account.domain.models;

import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;
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
        this(id, userId, type, status, currency, LocalDateTime.now(), LocalDateTime.now());
    }

    private Account(UUID id, UUID userId, AccountType type, AccountStatus status, Currency currency,
                    LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.status = status;
        this.currency = currency;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account restore(UUID id, UUID userId, AccountType type, AccountStatus status, Currency currency,
                                  LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new Account(id, userId, type, status, currency, createdAt, updatedAt);
    }

    public void close() {
        this.status = AccountStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }
}