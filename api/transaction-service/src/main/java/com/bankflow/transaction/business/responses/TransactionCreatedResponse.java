package com.bankflow.transaction.business.responses;

import com.bankflow.transaction.core.aggregates.TransactionAggregate;

import java.util.UUID;

public record TransactionCreatedResponse(
        UUID id,
        String message
) {
    public static TransactionCreatedResponse from(UUID id, String message) {
        return new TransactionCreatedResponse(
                id,
                message
        );
    }
}
