package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

public class InsufficientFundsException extends AppException {
    public InsufficientFundsException() {
        super("Insufficient funds", ErrorType.BUSINESS_RULE);
    }
}