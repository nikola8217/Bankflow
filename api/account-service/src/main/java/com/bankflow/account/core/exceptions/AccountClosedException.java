package com.bankflow.account.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AccountClosedException extends AppException {
    public AccountClosedException(UUID id) {
        super("Account is already closed: " + id, HttpStatus.BAD_REQUEST);
    }
}