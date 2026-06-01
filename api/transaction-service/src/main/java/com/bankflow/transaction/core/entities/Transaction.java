package com.bankflow.transaction.core.entities;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.core.enums.TransactionStatus;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Transaction {
    private final UUID id;
    private final UUID userId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final AccountSnapshot accountSnapshot;
    private TransactionStatus status;
    private final UUID targetAccountId;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Transaction(UUID id, UUID userId, TransactionType type, BigDecimal amount, AccountSnapshot accountSnapshot,
                       TransactionStatus status, UUID targetAccountId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.accountSnapshot = accountSnapshot;
        this.status = status;
        this.targetAccountId = targetAccountId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void complete() {
        this.status = TransactionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = TransactionStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }
}
