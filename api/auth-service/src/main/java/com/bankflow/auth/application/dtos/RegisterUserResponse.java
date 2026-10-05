package com.bankflow.auth.application.dtos;

import com.bankflow.auth.domain.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegisterUserResponse(
        UUID id,
        String email,
        String firstName,
        String lastName,
        boolean isActive,
        LocalDateTime createdAt
) {
    public static RegisterUserResponse from(User user) {
        return new RegisterUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}