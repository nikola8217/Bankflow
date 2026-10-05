package com.bankflow.auth.web.requests;

import com.bankflow.auth.application.dtos.LoginUser;
import com.bankflow.auth.web.requests.httpValidations.AuthRequestsValidation;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginUserRequest {
    private String email;
    private String password;

    public LoginUser format() {
        AuthRequestsValidation.validateLoginRequest(this);

        return new LoginUser(
                email,
                password
        );
    }
}
