package com.bankflow.transaction.application.commands;

import com.bankflow.transaction.application.bus.Command;

import java.util.UUID;

public record RecordAccountCommand(
        UUID accountId,
        UUID userId,
        String currency
) implements Command<Void> {}