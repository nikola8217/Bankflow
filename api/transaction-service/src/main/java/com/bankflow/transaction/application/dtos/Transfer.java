package com.bankflow.transaction.application.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record Transfer(
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String token,
        UUID userId,
        String idempotencyKey
) {}
