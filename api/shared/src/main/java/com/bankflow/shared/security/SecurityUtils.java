package com.bankflow.shared.security;

import java.util.Objects;
import java.util.UUID;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static UUID getCurrentUserId() {
        return UUID.fromString(
                (String) Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication().getPrincipal()
                )
        );
    }
}