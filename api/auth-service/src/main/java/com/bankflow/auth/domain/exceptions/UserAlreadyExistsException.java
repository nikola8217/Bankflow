package com.bankflow.auth.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

public class UserAlreadyExistsException extends AppException {
    public UserAlreadyExistsException(String email) {
        super("User already exists with email: " + email, ErrorType.CONFLICT);
    }
}