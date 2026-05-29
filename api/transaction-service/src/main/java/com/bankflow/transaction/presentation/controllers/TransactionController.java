package com.bankflow.transaction.presentation.controllers;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.responses.TransactionResponse;
import com.bankflow.transaction.business.services.TransactionService;
import com.bankflow.transaction.presentation.httpValidations.TransactionRequestsValidation;
import com.bankflow.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader("Authorization") String token,
            @RequestBody AccountTransactionDto dto) {

        TransactionRequestsValidation.validateAmount(dto);

        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(201).body(transactionService.deposit(dto, userId, token));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader("Authorization") String token,
            @RequestBody AccountTransactionDto dto) {

        TransactionRequestsValidation.validateAmount(dto);

        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(201).body(transactionService.withdraw(dto, userId, token));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader("Authorization") String token,
            @RequestBody TransferDto dto) {

        TransactionRequestsValidation.validateTransferRequest(dto);

        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(201).body(transactionService.transfer(dto, userId, token));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(@PathVariable UUID accountId) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountId));
    }
}