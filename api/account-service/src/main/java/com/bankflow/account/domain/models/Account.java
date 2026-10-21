package com.bankflow.account.domain.models;

import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;
import com.bankflow.account.domain.exceptions.AccountClosedException;
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

    /** Step one of the closing saga. Only an active account can start closing. */
    public void requestClose() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountClosedException(id);
        }

        if (status != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account " + id + " cannot start closing from " + status);
        }

        changeStatus(AccountStatus.CLOSING);
    }

    /** The Ledger approved: the account holds no money and accepts no more bookings. */
    public void confirmClose() {
        requireClosing();

        changeStatus(AccountStatus.CLOSED);
    }

    /** Compensation: the Ledger rejected the closure, so the account goes back to active. */
    public void cancelClose() {
        requireClosing();

        changeStatus(AccountStatus.ACTIVE);
    }

    public boolean isClosing() {
        return status == AccountStatus.CLOSING;
    }

    private void requireClosing() {
        if (status != AccountStatus.CLOSING) {
            throw new IllegalStateException("Account " + id + " is not closing but " + status);
        }
    }

    private void changeStatus(AccountStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}