package com.bankflow.auth.core.validations;

import com.bankflow.shared.exceptions.ValidationException;

public class UserValidation {
    public static void validateRequiredField(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field + " is required");
        }
    }

    public static void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePassword(String password) {
        if (password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters");
        }
    }
}