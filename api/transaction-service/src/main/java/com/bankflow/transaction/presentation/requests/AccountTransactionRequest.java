package com.bankflow.transaction.presentation.requests;

import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.presentation.httpValidations.TransactionRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class AccountTransactionRequest {
    private UUID accountId;
    private BigDecimal amount;

    public AccountTransactionDto format(String token, String idempotencyKey, UUID userId) {
        TransactionRequestsValidation.validateAmount(this);
        return new AccountTransactionDto(
                accountId,
                amount,
                token,
                userId,
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString()
        );
    }
}