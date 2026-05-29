package com.bankflow.account.presentation.controllers;

import com.bankflow.account.business.dtos.CreateAccountDto;
import com.bankflow.account.business.responses.AccountResponse;
import com.bankflow.account.business.services.AccountService;
import com.bankflow.account.presentation.httpValidations.AccountRequestValidation;
import com.bankflow.shared.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<AccountResponse> createAccount(@RequestBody CreateAccountDto dto) {
        AccountRequestValidation.validateCreateAccount(dto);

        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.status(201).body(accountService.createAccount(dto, userId));
    }

    @GetMapping("/user")
    public ResponseEntity<List<AccountResponse>> getUserAccounts() {
        UUID userId = SecurityUtils.getCurrentUserId();

        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.closeAccount(id));
    }
}