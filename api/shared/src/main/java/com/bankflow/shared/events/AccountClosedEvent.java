package com.bankflow.shared.events;

import java.util.UUID;

public record AccountClosedEvent(
        UUID accountId,
        UUID userId,
        String currency
) {}
