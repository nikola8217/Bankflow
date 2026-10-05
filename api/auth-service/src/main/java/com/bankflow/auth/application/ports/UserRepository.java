package com.bankflow.auth.application.ports;

import com.bankflow.auth.domain.models.User;

import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
}