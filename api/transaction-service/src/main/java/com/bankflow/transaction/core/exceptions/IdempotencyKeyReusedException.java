package com.bankflow.transaction.core.exceptions;

import org.springframework.http.HttpStatus;

public class IdempotencyKeyReusedException extends TransactionException {
    public IdempotencyKeyReusedException() {
        super("Idempotency-Key was already used for a different request", HttpStatus.UNPROCESSABLE_ENTITY);
    }
}