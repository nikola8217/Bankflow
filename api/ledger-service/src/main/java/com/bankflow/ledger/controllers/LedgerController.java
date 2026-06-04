package com.bankflow.ledger.presentation.controllers;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.exceptions.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final IBalanceRepository balanceRepository;
    private final ITransactionHistoryRepository historyRepository;

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Balance> getBalance(@PathVariable UUID accountId) {
        Balance balance = balanceRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(
                        "Balance not found for account: " + accountId,
                        HttpStatus.NOT_FOUND
                ) {});
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionHistory>> getHistory(@PathVariable UUID accountId) {
        return ResponseEntity.ok(historyRepository.findAllByAccountId(accountId));
    }
}