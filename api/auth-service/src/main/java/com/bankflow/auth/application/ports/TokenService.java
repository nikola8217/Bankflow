package com.bankflow.auth.application.ports;

import java.util.UUID;

public interface TokenService {
    String generateToken(UUID userId, String email);
}