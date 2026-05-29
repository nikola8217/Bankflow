package com.bankflow.auth.presentation.httpValidations;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.business.dtos.RegisterUserDto;
import com.bankflow.auth.core.validations.UserValidation;

public class AuthRequestsValidation {
    public static void validateRegisterRequest(RegisterUserDto dto) {
        UserValidation.validateRequiredField(dto.firstName(), "First name");
        UserValidation.validateRequiredField(dto.lastName(), "Last name");
        UserValidation.validateRequiredField(dto.email(), "Email");
        UserValidation.validateRequiredField(dto.password(), "Password");
        UserValidation.validateEmail(dto.email());
        UserValidation.validatePassword(dto.password());
    }

    public static void validateLoginRequest(LoginUserDto dto) {
        UserValidation.validateRequiredField(dto.email(), "Email");
        UserValidation.validateRequiredField(dto.password(), "Password");
    }
}
