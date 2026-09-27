package com.bankflow.transaction.presentation.httpValidations;

import com.bankflow.shared.exceptions.ValidationException;
import com.bankflow.transaction.presentation.requests.AccountTransactionRequest;
import com.bankflow.transaction.presentation.requests.TransferRequest;

import java.math.BigDecimal;

public class TransactionRequestsValidation {

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 255;

    private static final int MAX_DECIMAL_PLACES = 2;

    public static void validateIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null) {
            return;
        }
        if (idempotencyKey.isBlank() || idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new ValidationException("Idempotency-Key must be 1-" + MAX_IDEMPOTENCY_KEY_LENGTH + " characters");
        }
    }

    public static void validateAmount(AccountTransactionRequest request) {
        if (request.getAccountId() == null) {
            throw new ValidationException("Account id is required");
        }
        validateMoney(request.getAmount());
    }

    public static void validateTransferRequest(TransferRequest request) {
        if (request.getFromAccountId() == null) {
            throw new ValidationException("From account id is required");
        }

        if (request.getToAccountId() == null) {
            throw new ValidationException("To account id is required");
        }

        if (request.getFromAccountId().equals(request.getToAccountId())) {
            throw new ValidationException("Cannot transfer to the same account");
        }

        validateMoney(request.getAmount());
    }

    public static void validateMoney(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }

        if (amount.stripTrailingZeros().scale() > MAX_DECIMAL_PLACES) {
            throw new ValidationException("Amount can have at most " + MAX_DECIMAL_PLACES + " decimal places");
        }
    }
}