package com.bankflow.shared.exceptions;

public abstract class AppException extends RuntimeException {

    private final ErrorType type;

    protected AppException(String message, ErrorType type) {
        super(message);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }
}