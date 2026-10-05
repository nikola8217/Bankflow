package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.ErrorType;

public class IdempotencyKeyConflictException extends TransactionException {
    public IdempotencyKeyConflictException() {
        super("A request with this Idempotency-Key is already being processed", ErrorType.CONFLICT);
    }
}