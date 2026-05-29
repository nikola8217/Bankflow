package com.bankflow.auth.business.dtos;

import com.bankflow.auth.core.validations.UserValidation;

public record LoginUserDto(
        String email,
        String password
) {}