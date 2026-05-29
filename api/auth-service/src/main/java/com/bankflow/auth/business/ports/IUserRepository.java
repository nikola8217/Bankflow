package com.bankflow.auth.business.ports;

import com.bankflow.auth.core.entities.User;

import java.util.Optional;

public interface IUserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
}