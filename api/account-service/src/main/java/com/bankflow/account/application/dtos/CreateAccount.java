package com.bankflow.account.application.dtos;

import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;

import java.util.UUID;

public record CreateAccount(
        UUID userId,
        AccountType type,
        Currency currency
) {}
