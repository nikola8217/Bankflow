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
import com.bankflow.transaction.presentation.requests.AccountTransactionRequest;
import com.bankflow.transaction.presentation.requests.TransferRequest;
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
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody AccountTransactionRequest request) {

        return ResponseEntity.status(201).body(
                commandBus.send(new DepositCommand(
                        request.format(token, idempotencyKey, SecurityUtils.getCurrentUserId())
                ))
        );
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionCreatedResponse> withdraw(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody AccountTransactionRequest request) {

        return ResponseEntity.status(201).body(
                commandBus.send(new WithdrawCommand(
                        request.format(token, idempotencyKey, SecurityUtils.getCurrentUserId())
                ))
        );
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader("Authorization") String token,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequest request) {

        return ResponseEntity.status(201).body(
                commandBus.send(new TransferCommand(
                        request.format(token, idempotencyKey, SecurityUtils.getCurrentUserId())
                ))
        );
    }
}