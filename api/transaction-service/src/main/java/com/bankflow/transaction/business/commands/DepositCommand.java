package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.core.commands.Command;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;

import java.util.UUID;

public record DepositCommand(
        AccountTransactionDto dto,
        UUID userId,
        String token
) implements Command<TransactionCreatedResponse> {}