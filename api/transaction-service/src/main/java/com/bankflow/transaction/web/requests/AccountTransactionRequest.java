package com.bankflow.transaction.web.requests;

import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.web.requests.validations.TransactionRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class AccountTransactionRequest {
    private UUID accountId;
    private BigDecimal amount;

    public AccountTransaction format(String token, String idempotencyKey, UUID userId) {
        TransactionRequestsValidation.validateAmount(this);
        TransactionRequestsValidation.validateIdempotencyKey(idempotencyKey);
        return new AccountTransaction(
                accountId,
                amount,
                token,
                userId,
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString()
        );
    }
}