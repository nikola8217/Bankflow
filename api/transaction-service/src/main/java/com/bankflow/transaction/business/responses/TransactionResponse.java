package com.bankflow.transaction.business.responses;

import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.core.enums.TransactionStatus;
import com.bankflow.transaction.core.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID userId,
        TransactionType type,
        BigDecimal amount,
        AccountSnapshot accountSnapshot,
        UUID targetAccountId,
        TransactionStatus status,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getUserId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getAccountSnapshot(),
                transaction.getTargetAccountId(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}