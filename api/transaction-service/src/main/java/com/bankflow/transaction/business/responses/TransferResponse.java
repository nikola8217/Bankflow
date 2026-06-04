package com.bankflow.transaction.business.responses;

import java.util.UUID;

public record TransferResponse(
        UUID transactionFromId,
        UUID transactionToId,
        String message
) {
    public static TransferResponse from(UUID transactionFromId, UUID transactionToId, String message) {
        return new TransferResponse(
                transactionFromId,
                transactionToId,
                message
        );
    }
}
