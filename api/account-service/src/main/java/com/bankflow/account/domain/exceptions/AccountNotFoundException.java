package com.bankflow.account.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

import java.util.UUID;

public class AccountNotFoundException extends AppException {
    public AccountNotFoundException(UUID id) {
        super("Account not found with id: " + id, ErrorType.NOT_FOUND);
    }
}