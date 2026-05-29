package com.bankflow.auth.core.exceptions;

import com.bankflow.shared.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}