package com.bankflow.transaction.application.idempotency;

import java.time.LocalDateTime;
import java.util.UUID;

public record IdempotencyRecord(
        UUID userId,
        String idempotencyKey,
        String requestFingerprint,
        UUID transactionId,
        LocalDateTime createdAt
) {
    public boolean isSameRequestAs(String fingerprint) {
        return requestFingerprint.equals(fingerprint);
    }
}