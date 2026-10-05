package com.bankflow.transaction.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransaction(
        UUID accountId,
        BigDecimal amount,
        String token,
        UUID userID,
        String idempotencyKey
) {}
