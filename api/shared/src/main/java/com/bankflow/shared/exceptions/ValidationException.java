package com.bankflow.shared.exceptions;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message, ErrorType.INVALID_REQUEST);
    }
}