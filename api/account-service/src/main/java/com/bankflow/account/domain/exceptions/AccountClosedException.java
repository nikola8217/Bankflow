package com.bankflow.account.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class AccountClosedException extends AppException {
    public AccountClosedException(UUID id) {
        super("Account is already closed: " + id, ErrorType.BUSINESS_RULE);
    }
}