package com.bankflow.account.presentation.httpValidations;

import com.bankflow.account.business.dtos.CreateAccountDto;
import com.bankflow.shared.exceptions.ValidationException;

public class AccountRequestValidation {

    public static void validateCreateAccount(CreateAccountDto dto) {
        if (dto.type() == null) {
            throw new ValidationException("Account type is required");
        }
        if (dto.currency() == null) {
            throw new ValidationException("Currency is required");
        }
    }
}