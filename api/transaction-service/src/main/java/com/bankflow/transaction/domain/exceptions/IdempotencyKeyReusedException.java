package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.ErrorType;

public class IdempotencyKeyReusedException extends TransactionException {
    public IdempotencyKeyReusedException() {
        super("Idempotency-Key was already used for a different request", ErrorType.UNPROCESSABLE);
    }
}