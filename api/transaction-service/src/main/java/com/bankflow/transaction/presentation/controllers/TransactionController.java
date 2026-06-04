package com.bankflow.transaction.presentation.controllers;

import com.bankflow.transaction.business.CommandBus;
import com.bankflow.transaction.business.commands.DepositCommand;
import com.bankflow.transaction.business.commands.TransferCommand;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.business.responses.TransferResponse;
import com.bankflow.transaction.presentation.httpValidations.TransactionRequestsValidation;
import com.bankflow.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CommandBus commandBus;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionCreatedResponse> deposit(
            @RequestHeader("Authorization") String token,
            @RequestBody AccountTransactionDto dto) {

        TransactionRequestsValidation.validateAmount(dto);
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(
                commandBus.send(new DepositCommand(dto, userId, token))
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionCreatedResponse> withdraw(
            @RequestHeader("Authorization") String token,
            @RequestBody AccountTransactionDto dto) {

        TransactionRequestsValidation.validateAmount(dto);
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(
                commandBus.send(new WithdrawCommand(dto, userId, token))
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("Authorization") String token,
            @RequestBody TransferDto dto) {

        TransactionRequestsValidation.validateTransferRequest(dto);
        UUID userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.status(201).body(
                commandBus.send(new TransferCommand(dto, userId, token))
        );
    }
}