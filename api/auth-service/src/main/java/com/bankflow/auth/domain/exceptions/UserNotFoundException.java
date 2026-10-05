package com.bankflow.auth.domain.exceptions;

import com.bankflow.shared.exceptions.AppException;
import com.bankflow.shared.exceptions.ErrorType;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String message) {
        super(message, ErrorType.UNAUTHORIZED);
    }
}