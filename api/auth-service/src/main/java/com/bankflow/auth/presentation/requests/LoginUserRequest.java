package com.bankflow.auth.presentation.requests;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.presentation.httpValidations.AuthRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginUserRequest {
    private String email;
    private String password;

    public LoginUserDto format() {
        AuthRequestsValidation.validateLoginRequest(this);

        return new LoginUserDto(
                email,
                password
        );
    }
}
