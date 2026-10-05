package com.bankflow.transaction.application.dtos;

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
