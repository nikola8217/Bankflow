package com.bankflow.auth.business.services;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.business.dtos.RegisterUserDto;
import com.bankflow.auth.business.dtos.TokenDto;
import com.bankflow.auth.business.ports.ITokenService;
import com.bankflow.auth.business.ports.IUserRepository;
import com.bankflow.auth.business.responses.RegisterUserResponse;
import com.bankflow.auth.core.entities.User;
import com.bankflow.auth.core.exceptions.UserAlreadyExistsException;
import com.bankflow.auth.core.exceptions.UserNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final IUserRepository userRepository;
    private final ITokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(IUserRepository userRepository, ITokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    public RegisterUserResponse register(RegisterUserDto dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new UserAlreadyExistsException(dto.email());
        }

        String hashedPassword = passwordEncoder.encode(dto.password());

        User user = new User(
                UUID.randomUUID(),
                dto.email(),
                hashedPassword,
                dto.firstName(),
                dto.lastName()
        );

        return RegisterUserResponse.from(userRepository.save(user));
    }

    public TokenDto login(LoginUserDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new UserNotFoundException("Invalid email"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new UserNotFoundException("Invalid password");
        }

        String token = tokenService.generateToken(user.getId(), user.getEmail());

        return new TokenDto(token);
    }
}