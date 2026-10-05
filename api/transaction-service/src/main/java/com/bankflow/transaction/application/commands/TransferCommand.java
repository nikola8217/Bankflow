package com.bankflow.transaction.application.commands;

import com.bankflow.transaction.application.bus.Command;
import com.bankflow.transaction.application.dtos.Transfer;
import com.bankflow.transaction.application.dtos.TransferResponse;

public record TransferCommand(Transfer dto) implements Command<TransferResponse> {}