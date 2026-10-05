package com.bankflow.auth.application.dtos;

public record LoginUser(
        String email,
        String password
) {}