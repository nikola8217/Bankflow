package com.bankflow.transaction.business.responses;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionStatusResponse(
        UUID id,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        String currency,
        UUID accountId,
        UUID targetAccountId,
        String failureReason
) {
    public static TransactionStatusResponse from(TransactionAggregate aggregate) {
        return new TransactionStatusResponse(
                aggregate.getTransactionId(),
                aggregate.getType(),
                aggregate.getStatus(),
                aggregate.getAmount(),
                aggregate.getCurrency(),
                aggregate.getAccountId(),
                aggregate.getTargetAccountId(),
                aggregate.getFailureReason()
        );
    }
}