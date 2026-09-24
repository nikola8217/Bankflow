package com.bankflow.ledger.controllers;

import com.bankflow.ledger.business.services.LedgerQueryService;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerQueryService queryService;

    @GetMapping("/balance/{accountId}")
    public ResponseEntity<Balance> getBalance(@PathVariable UUID accountId) {
        return ResponseEntity.ok(queryService.getBalance(accountId, SecurityUtils.getCurrentUserId()));
    }

    @GetMapping("/history/{accountId}")
    public ResponseEntity<List<TransactionHistory>> getHistory(@PathVariable UUID accountId) {
        return ResponseEntity.ok(queryService.getHistory(accountId, SecurityUtils.getCurrentUserId()));
    }
}