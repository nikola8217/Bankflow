package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.core.commands.Command;
import com.bankflow.transaction.business.responses.TransferResponse;

public record TransferCommand(TransferDto dto) implements Command<TransferResponse> {}