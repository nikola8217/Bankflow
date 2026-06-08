package com.bankflow.auth.presentation.controllers;

import com.bankflow.auth.business.responses.LoginUserResponse;
import com.bankflow.auth.business.responses.RegisterUserResponse;
import com.bankflow.auth.business.services.AuthService;
import com.bankflow.auth.presentation.requests.LoginUserRequest;
import com.bankflow.auth.presentation.requests.RegisterUserRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterUserResponse> register(@RequestBody RegisterUserRequest request) {
        RegisterUserResponse user = authService.register(request.format());

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(@RequestBody LoginUserRequest request) {
        LoginUserResponse token = authService.login(request.format());

        return ResponseEntity.ok(token);
    }
}