package com.bankflow.transaction.domain.models;

import java.util.UUID;

public record AccountSnapshot(
        UUID id,
        UUID userId,
        String type,
        String currency,
        String status
) {}