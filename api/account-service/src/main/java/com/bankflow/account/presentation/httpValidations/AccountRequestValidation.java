package com.bankflow.account.presentation.httpValidations;

import com.bankflow.account.presentation.requests.CreateAccountRequest;
import com.bankflow.shared.exceptions.ValidationException;

public class AccountRequestValidation {

    public static void validateCreateAccount(CreateAccountRequest request) {
        if (request.getType() == null) {
            throw new ValidationException("Account type is required");
        }
        if (request.getCurrency() == null) {
            throw new ValidationException("Currency is required");
        }
    }
}