package com.bankflow.transaction.presentation.requests;

import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.presentation.httpValidations.TransactionRequestsValidation;
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

    public TransferDto format(String token, String idempotencyKey, UUID userId) {
        TransactionRequestsValidation.validateTransferRequest(this);
        return new TransferDto(
                fromAccountId,
                toAccountId,
                amount,
                token,
                userId,
                idempotencyKey != null ? idempotencyKey : UUID.randomUUID().toString()
        );
    }
}