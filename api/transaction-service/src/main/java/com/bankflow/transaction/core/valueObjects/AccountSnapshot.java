package com.bankflow.transaction.core.valueObjects;

import java.util.UUID;

public record AccountSnapshot(
        UUID id,
        String type,
        String currency,
        String status
) {}