package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class AccountConflictException extends AppException {
    public AccountConflictException() {
        super("Account conflict", HttpStatus.BAD_REQUEST);
    }
}
