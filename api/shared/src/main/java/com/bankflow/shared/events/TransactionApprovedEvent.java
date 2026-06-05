package com.bankflow.shared.events;

import com.bankflow.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionApprovedEvent(
        UUID transactionId,
        UUID accountId,
        UUID userId,
        TransactionType type,
        BigDecimal amount,
        String currency,
        UUID targetAccountId,
        LocalDateTime createdAt
) {}