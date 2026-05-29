package com.bankflow.account.business.dtos;

import com.bankflow.account.core.enums.AccountType;
import com.bankflow.account.core.enums.Currency;

import java.util.UUID;

public record CreateAccountDto(
        UUID userId,
        AccountType type,
        Currency currency
) {}
