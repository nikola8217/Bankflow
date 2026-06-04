package com.bankflow.ledger.core.entities;

import com.bankflow.shared.enums.TransactionType;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class TransactionHistory {
    private final UUID transactionId;
    private final UUID accountId;
    private final UUID userId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final String currency;
    private final UUID targetAccountId;
    private final LocalDateTime createdAt;

    public TransactionHistory(UUID transactionId, UUID accountId, UUID userId,
                              TransactionType type, BigDecimal amount,
                              String currency, UUID targetAccountId,
                              LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.targetAccountId = targetAccountId;
        this.createdAt = createdAt;
    }
}