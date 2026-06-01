package com.bankflow.balance.core.entities;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Balance {

    private final UUID id;
    private final UUID accountId;
    private BigDecimal amount;
    private final String currency;
    private LocalDateTime updatedAt;

    public Balance(UUID id, UUID accountId, BigDecimal amount, String currency, LocalDateTime updatedAt) {
        this.id = id;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.updatedAt = updatedAt;
    }

    public void deposit(BigDecimal amount) {
        this.amount = this.amount.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void withdraw(BigDecimal amount) {
        this.amount = this.amount.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
}