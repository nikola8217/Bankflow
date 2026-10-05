package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class ConcurrencyConflictException extends TransactionException {
    public ConcurrencyConflictException(UUID transactionId) {
        super("Transaction " + transactionId + " was changed concurrently", ErrorType.CONFLICT);
    }
}