package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.core.commands.Command;

import java.util.UUID;

public record FailTransactionCommand(
        UUID transactionId,
        String reason
) implements Command<Void> {}