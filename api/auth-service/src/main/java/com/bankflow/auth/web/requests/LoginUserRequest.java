package com.bankflow.auth.web.requests;

import com.bankflow.auth.application.dtos.LoginUser;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginUserRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginUser format() {
        return new LoginUser(
                email,
                password
        );
    }
}