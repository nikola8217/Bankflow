package com.bankflow.auth.web.requests.httpValidations;

import com.bankflow.auth.domain.validations.UserValidation;
import com.bankflow.auth.web.requests.LoginUserRequest;
import com.bankflow.auth.web.requests.RegisterUserRequest;

public class AuthRequestsValidation {
    public static void validateRegisterRequest(RegisterUserRequest request) {
        UserValidation.validateRequiredField(request.getFirstName(), "First name");
        UserValidation.validateRequiredField(request.getLastName(), "Last name");
        UserValidation.validateRequiredField(request.getEmail(), "Email");
        UserValidation.validateRequiredField(request.getPassword(), "Password");
        UserValidation.validateEmail(request.getEmail());
        UserValidation.validatePassword(request.getPassword());
    }

    public static void validateLoginRequest(LoginUserRequest request) {
        UserValidation.validateRequiredField(request.getEmail(), "Email");
        UserValidation.validateRequiredField(request.getPassword(), "Password");
    }
}
