package com.bankflow.shared.events;

import java.util.UUID;

public record AccountCreatedEvent(
        UUID accountId,
        UUID userId,
        String currency
) {}