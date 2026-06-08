package com.bankflow.transaction.business.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountTransactionDto(
        UUID accountId,
        BigDecimal amount,
        String token,
        UUID userID,
        String idempotencyKey
) {}
