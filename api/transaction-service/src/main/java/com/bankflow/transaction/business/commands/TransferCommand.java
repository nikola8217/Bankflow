package com.bankflow.transaction.business.commands;

import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.core.commands.Command;
import com.bankflow.transaction.business.responses.TransferResponse;

import java.util.UUID;

public record TransferCommand(
        TransferDto dto,
        UUID userId,
        String token
) implements Command<TransferResponse> {}