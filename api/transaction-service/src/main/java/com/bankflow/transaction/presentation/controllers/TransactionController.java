package com.bankflow.transaction.presentation.controllers;

import com.bankflow.shared.security.SecurityUtils;
import com.bankflow.transaction.business.CommandBus;
import com.bankflow.transaction.business.services.TransactionQueryService;
import com.bankflow.transaction.business.commands.DepositCommand;
import com.bankflow.transaction.business.commands.TransferCommand;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.business.responses.TransactionStatusResponse;
import com.bankflow.transaction.business.responses.TransferResponse;
import com.bankflow.transaction.presentation.requests.AccountTransactionRequest;
import com.bankflow.transaction.presentation.requests.TransferRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final CommandBus commandBus;
    private final TransactionQueryService queryService;

    @PostMapping("/deposit")
    public ResponseEntity<TransactionCreatedResponse> deposit(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody AccountTransactionRequest request) {

        TransactionCreatedResponse response = commandBus.send(new DepositCommand(
                request.format(SecurityUtils.getToken(), idempotencyKey, SecurityUtils.getCurrentUserId())
        ));
        return ResponseEntity.accepted().location(statusUrl(response.id())).body(response);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionCreatedResponse> withdraw(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody AccountTransactionRequest request) {

        TransactionCreatedResponse response = commandBus.send(new WithdrawCommand(
                request.format(SecurityUtils.getToken(), idempotencyKey, SecurityUtils.getCurrentUserId())
        ));
        return ResponseEntity.accepted().location(statusUrl(response.id())).body(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> transfer(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody TransferRequest request) {

        TransferResponse response = commandBus.send(new TransferCommand(
                request.format(SecurityUtils.getToken(), idempotencyKey, SecurityUtils.getCurrentUserId())
        ));
        return ResponseEntity.accepted().location(statusUrl(response.transactionFromId())).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionStatusResponse> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(queryService.getTransaction(id, SecurityUtils.getCurrentUserId()));
    }

    private URI statusUrl(UUID transactionId) {
        return URI.create("/api/transactions/" + transactionId);
    }
}