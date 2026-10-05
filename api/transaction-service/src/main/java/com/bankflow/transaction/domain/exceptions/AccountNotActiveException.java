package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class AccountNotActiveException extends AppException {
    public AccountNotActiveException(UUID id) {
        super("Account is not active: " + id, ErrorType.BUSINESS_RULE);
    }
}