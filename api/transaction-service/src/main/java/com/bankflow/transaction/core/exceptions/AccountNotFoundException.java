package com.bankflow.transaction.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;
import java.util.UUID;

public class AccountNotFoundException extends AppException {
    public AccountNotFoundException(UUID id) {
        super("Account not found with id: " + id, HttpStatus.NOT_FOUND);
    }
}