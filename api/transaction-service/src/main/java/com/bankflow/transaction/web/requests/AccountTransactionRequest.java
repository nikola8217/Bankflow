package com.bankflow.transaction.web.requests;

import com.bankflow.transaction.application.dtos.AccountTransaction;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class AccountTransactionRequest {

    @NotNull(message = "Account id is required")
    private UUID accountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 36, fraction = 2, message = "Amount can have at most 2 decimal places")
    private BigDecimal amount;

    public AccountTransaction format(String token, String idempotencyKey, UUID userId) {
        return new AccountTransaction(
                accountId,
                amount,
                token,
                userId,
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString()
        );
    }
}