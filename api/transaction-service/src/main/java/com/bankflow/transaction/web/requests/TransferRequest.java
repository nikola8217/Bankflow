package com.bankflow.transaction.web.requests;

import com.bankflow.transaction.application.dtos.Transfer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class TransferRequest {

    @NotNull(message = "From account id is required")
    private UUID fromAccountId;

    @NotNull(message = "To account id is required")
    private UUID toAccountId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than zero")
    @Digits(integer = 36, fraction = 2, message = "Amount can have at most 2 decimal places")
    private BigDecimal amount;

    @JsonIgnore
    @AssertTrue(message = "Cannot transfer to the same account")
    public boolean isDifferentAccounts() {
        return fromAccountId == null || !fromAccountId.equals(toAccountId);
    }

    public Transfer format(String token, String idempotencyKey, UUID userId) {
        return new Transfer(
                fromAccountId,
                toAccountId,
                amount,
                token,
                userId,
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString()
        );
    }
}