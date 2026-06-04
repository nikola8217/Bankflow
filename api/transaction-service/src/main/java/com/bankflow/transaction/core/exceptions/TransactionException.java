package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class TransactionException extends AppException {
    public TransactionException(String message, HttpStatus status) {
        super(message, status);
    }
}