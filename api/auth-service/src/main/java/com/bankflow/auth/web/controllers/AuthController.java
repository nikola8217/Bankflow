package com.bankflow.auth.web.controllers;

import com.bankflow.auth.application.dtos.LoginUserResponse;
import com.bankflow.auth.application.dtos.RegisterUserResponse;
import com.bankflow.auth.application.services.AuthService;
import com.bankflow.auth.web.requests.LoginUserRequest;
import com.bankflow.auth.web.requests.RegisterUserRequest;
import jakarta.validation.Valid;
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
    public ResponseEntity<RegisterUserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserResponse user = authService.register(request.format());

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(@Valid @RequestBody LoginUserRequest request) {
        LoginUserResponse token = authService.login(request.format());

        return ResponseEntity.ok(token);
    }
}