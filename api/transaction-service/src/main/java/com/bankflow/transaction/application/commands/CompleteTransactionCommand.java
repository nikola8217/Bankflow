package com.bankflow.transaction.application.commands;

import com.bankflow.transaction.application.bus.Command;

import java.util.UUID;

public record CompleteTransactionCommand(
        UUID transactionId
) implements Command<Void> {}