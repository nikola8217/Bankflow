package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.core.commands.Command;

import java.util.UUID;

public record CompleteTransactionCommand(
        UUID transactionId
) implements Command<Void> {}