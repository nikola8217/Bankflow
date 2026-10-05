package com.bankflow.transaction.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

public class TransactionException extends AppException {
    public TransactionException(String message, ErrorType type) {
        super(message, type);
    }
}