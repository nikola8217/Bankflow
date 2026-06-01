package com.bankflow.balance.business.responses;

import com.bankflow.balance.core.entities.Balance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BalanceResponse(
        UUID accountId,
        BigDecimal amount,
        String currency,
        LocalDateTime updatedAt
) {
    public static BalanceResponse from(Balance balance) {
        return new BalanceResponse(
                balance.getAccountId(),
                balance.getAmount(),
                balance.getCurrency(),
                balance.getUpdatedAt()
        );
    }
}