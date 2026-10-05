package com.bankflow.ledger.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class BalanceNotFoundException extends AppException {
    public BalanceNotFoundException(UUID accountId) {
        super("Balance not found for account: " + accountId, ErrorType.NOT_FOUND);
    }
}