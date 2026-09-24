package com.bankflow.account.presentation.controllers;

import com.bankflow.account.business.responses.AccountResponse;
import com.bankflow.account.business.services.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/accounts")
public class InternalAccountController {

    private final AccountService accountService;

    public InternalAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.getAccountInternal(id));
    }
}