package com.bankflow.auth.business.dtos;

public record TokenDto(String accessToken, String tokenType) {
    public TokenDto(String accessToken) {
        this(accessToken, "Bearer");
    }
}