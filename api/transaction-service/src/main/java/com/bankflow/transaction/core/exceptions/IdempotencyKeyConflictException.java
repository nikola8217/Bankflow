package com.bankflow.transaction.core.exceptions;

import org.springframework.http.HttpStatus;

public class IdempotencyKeyConflictException extends TransactionException {
    public IdempotencyKeyConflictException() {
        super("A request with this Idempotency-Key is already being processed", HttpStatus.CONFLICT);
    }
}