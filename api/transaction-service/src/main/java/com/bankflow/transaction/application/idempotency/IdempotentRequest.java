package com.bankflow.transaction.application.idempotency;

import com.bankflow.shared.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record IdempotentRequest(UUID userId, String key, String fingerprint) {

    public static IdempotentRequest of(UUID userId, String key, TransactionType type,
                                       UUID accountId, UUID targetAccountId, BigDecimal amount) {

        String fingerprint = type + "|" + accountId + "|" + targetAccountId + "|"
                + amount.stripTrailingZeros().toPlainString();
        return new IdempotentRequest(userId, key, fingerprint);
    }
}