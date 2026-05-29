package com.bankflow.account.business.responses;

import com.bankflow.account.core.entities.Account;
import com.bankflow.account.core.enums.AccountStatus;
import com.bankflow.account.core.enums.AccountType;
import com.bankflow.account.core.enums.Currency;

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