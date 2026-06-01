package com.bankflow.transaction.core.events;

import com.bankflow.transaction.core.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID transactionId,
        UUID accountId,
        UUID userId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        UUID targetAccountId,
        LocalDateTime createdAt
) {}
