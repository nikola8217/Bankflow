package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.core.commands.Command;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;

public record WithdrawCommand(AccountTransactionDto dto) implements Command<TransactionCreatedResponse> {}