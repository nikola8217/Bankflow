package com.bankflow.ledger.domain.models;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Balance {
    private final UUID userId;
    private final UUID accountId;
    private BigDecimal amount;
    private final String currency;
    private LocalDateTime updatedAt;
    private boolean closed;

    public Balance(UUID userId, UUID accountId, BigDecimal amount, String currency) {
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.updatedAt = LocalDateTime.now();
    }

    private Balance(UUID userId, UUID accountId, BigDecimal amount, String currency,
                    LocalDateTime updatedAt, boolean closed) {
        this.userId = userId;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.updatedAt = updatedAt;
        this.closed = closed;
    }

    public static Balance restore(UUID userId, UUID accountId, BigDecimal amount,
                                  String currency, LocalDateTime updatedAt, boolean closed) {
        return new Balance(userId, accountId, amount, currency, updatedAt, closed);
    }

    /** Only an empty account can be closed: closing must never strand or destroy money. */
    public boolean canClose() {
        return amount.signum() == 0;
    }

    public void close() {
        if (!canClose()) {
            throw new IllegalStateException("Account " + accountId + " still holds " + amount + " " + currency);
        }
        this.closed = true;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canDebit(BigDecimal amount) {
        return this.amount.compareTo(amount) >= 0;
    }

    public void credit(BigDecimal amount) {
        this.amount = this.amount.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void debit(BigDecimal amount) {
        if (!canDebit(amount)) {
            throw new IllegalStateException("Insufficient funds for account " + accountId);
        }
        this.amount = this.amount.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
}