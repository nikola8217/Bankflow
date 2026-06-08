package com.bankflow.auth.presentation.requests;

import com.bankflow.auth.business.dtos.RegisterUserDto;
import com.bankflow.auth.presentation.httpValidations.AuthRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterUserRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;

    public RegisterUserDto format() {
        AuthRequestsValidation.validateRegisterRequest(this);

        return new RegisterUserDto(
                email,
                password,
                firstName,
                lastName
        );
    }
}
