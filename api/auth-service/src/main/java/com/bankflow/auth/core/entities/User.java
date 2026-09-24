package com.bankflow.auth.core.entities;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class User {

    private final UUID id;
    private final String email;
    private final String password;
    private final String firstName;
    private final String lastName;
    private final boolean isActive;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public User(UUID id, String email, String password, String firstName, String lastName) {
        this(id, email, password, firstName, lastName, true, LocalDateTime.now(), LocalDateTime.now());
    }

    private User(UUID id, String email, String password, String firstName, String lastName,
                 boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User restore(UUID id, String email, String password, String firstName, String lastName,
                               boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(id, email, password, firstName, lastName, isActive, createdAt, updatedAt);
    }
}