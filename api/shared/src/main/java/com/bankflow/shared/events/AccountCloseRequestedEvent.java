package com.bankflow.shared.events;

import java.util.UUID;

public record AccountCloseRequestedEvent(
        UUID accountId,
        UUID userId
) {}