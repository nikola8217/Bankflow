package com.bankflow.account.web.controllers;

import com.bankflow.account.application.dtos.AccountResponse;
import com.bankflow.account.application.services.AccountService;
import com.bankflow.account.web.requests.CreateAccountRequest;
import com.bankflow.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse account = accountService.createAccount(request.format(SecurityUtils.getCurrentUserId()));

        return ResponseEntity.status(201).body(account);
    }

    @GetMapping("/user")
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(id, SecurityUtils.getCurrentUserId()));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.closeAccount(id, SecurityUtils.getCurrentUserId()));
    }
}