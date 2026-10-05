package com.bankflow.account.application.dtos;

import com.bankflow.account.domain.models.Account;
import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID userId,
        AccountType type,
        Currency currency,
        AccountStatus status,
        LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getUserId(),
                account.getType(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}