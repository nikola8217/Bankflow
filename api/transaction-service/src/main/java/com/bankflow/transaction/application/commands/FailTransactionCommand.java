package com.bankflow.transaction.application.commands;

import com.bankflow.transaction.application.bus.Command;

import java.util.UUID;

public record FailTransactionCommand(
        UUID transactionId,
        String reason
) implements Command<Void> {}