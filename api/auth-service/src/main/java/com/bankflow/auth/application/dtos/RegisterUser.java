package com.bankflow.auth.application.dtos;

public record RegisterUser(
        String email,
        String password,
        String firstName,
        String lastName
) {}