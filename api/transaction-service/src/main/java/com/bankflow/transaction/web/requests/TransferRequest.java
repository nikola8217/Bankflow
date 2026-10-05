package com.bankflow.transaction.web.requests;

import com.bankflow.transaction.application.dtos.Transfer;
import com.bankflow.transaction.web.requests.validations.TransactionRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class TransferRequest {
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;

    public Transfer format(String token, String idempotencyKey, UUID userId) {
        TransactionRequestsValidation.validateTransferRequest(this);
        TransactionRequestsValidation.validateIdempotencyKey(idempotencyKey);
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