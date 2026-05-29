package com.bankflow.auth.presentation.controllers;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.business.dtos.RegisterUserDto;
import com.bankflow.auth.business.dtos.TokenDto;
import com.bankflow.auth.business.responses.RegisterUserResponse;
import com.bankflow.auth.business.services.AuthService;
import com.bankflow.auth.presentation.httpValidations.AuthRequestsValidation;
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
    public ResponseEntity<?> register(@RequestBody RegisterUserDto dto) {
        AuthRequestsValidation.validateRegisterRequest(dto);

        RegisterUserResponse user = authService.register(dto);

        return ResponseEntity.status(201).body(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserDto dto) {
        AuthRequestsValidation.validateLoginRequest(dto);

        TokenDto token = authService.login(dto);

        return ResponseEntity.ok(token);
    }
}