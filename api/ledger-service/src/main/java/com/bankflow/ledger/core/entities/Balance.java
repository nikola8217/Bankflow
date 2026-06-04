package com.bankflow.ledger.core.entities;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Balance {
    private final UUID accountId;
    private BigDecimal amount;
    private final String currency;
    private LocalDateTime updatedAt;

    public Balance(UUID accountId, BigDecimal amount, String currency) {
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.updatedAt = LocalDateTime.now();
    }

    public void credit(BigDecimal amount) {
        this.amount = this.amount.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void debit(BigDecimal amount) {
        this.amount = this.amount.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }
}