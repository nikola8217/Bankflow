package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

public class AccountConflictException extends AppException {
    public AccountConflictException() {
        super("Account conflict", ErrorType.BUSINESS_RULE);
    }
}