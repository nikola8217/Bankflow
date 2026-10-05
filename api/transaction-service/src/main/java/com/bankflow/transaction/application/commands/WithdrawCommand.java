package com.bankflow.transaction.application.commands;

import com.bankflow.transaction.application.bus.Command;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.dtos.TransactionCreatedResponse;

public record WithdrawCommand(AccountTransaction dto) implements Command<TransactionCreatedResponse> {}