package com.bankflow.auth.web.requests;

import com.bankflow.auth.application.dtos.RegisterUser;
import com.bankflow.auth.web.requests.validations.AuthRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterUserRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public RegisterUser format() {
        AuthRequestsValidation.validateRegisterRequest(this);

        return new RegisterUser(
                email,
                password,
                firstName,
                lastName
        );
    }
}
