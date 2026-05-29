package com.bankflow.auth.business.ports;

import java.util.UUID;

public interface ITokenService {
    String generateToken(UUID userId, String email);
    boolean isTokenValid(String token);
}