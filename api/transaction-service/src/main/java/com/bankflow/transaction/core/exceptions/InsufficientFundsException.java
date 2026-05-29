package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends AppException {
    public InsufficientFundsException() {
        super("Insufficient funds", HttpStatus.BAD_REQUEST);
    }
}