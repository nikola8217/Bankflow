package com.bankflow.auth.business.responses;

public record LoginUserResponse(
        String token,
        String tokenType
) {
    public static LoginUserResponse from(String token) {
        return new LoginUserResponse(token, "Bearer");
    }
}
