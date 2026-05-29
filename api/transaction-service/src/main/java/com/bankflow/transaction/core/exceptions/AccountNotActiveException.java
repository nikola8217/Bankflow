package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AccountNotActiveException extends AppException {
    public AccountNotActiveException(UUID id) {
        super("Account is not active: " + id, HttpStatus.BAD_REQUEST);
    }
}