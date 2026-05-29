package com.bankflow.auth.business.dtos;

import com.bankflow.auth.core.validations.UserValidation;

public record RegisterUserDto(
        String email,
        String password,
        String firstName,
        String lastName
) {}