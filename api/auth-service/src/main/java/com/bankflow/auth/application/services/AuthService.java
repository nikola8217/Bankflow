package com.bankflow.auth.application.services;

import com.bankflow.auth.application.dtos.LoginUser;
import com.bankflow.auth.application.dtos.RegisterUser;
import com.bankflow.auth.application.ports.TokenService;
import com.bankflow.auth.application.ports.UserRepository;
import com.bankflow.auth.application.dtos.LoginUserResponse;
import com.bankflow.auth.application.dtos.RegisterUserResponse;
import com.bankflow.auth.domain.models.User;
import com.bankflow.auth.domain.exceptions.InvalidCredentialsException;
import com.bankflow.auth.domain.exceptions.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AuthService(UserRepository userRepository, TokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("timing-attack-protection");
    }

    public RegisterUserResponse register(RegisterUser dto) {
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

    public LoginUserResponse login(LoginUser dto) {
        Optional<User> user = userRepository.findByEmail(dto.email());

        String hash = user.map(User::getPassword).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(dto.password(), hash);

        if (user.isEmpty() || !passwordMatches || !user.get().isActive()) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.generateToken(user.get().getId(), user.get().getEmail());

        return LoginUserResponse.from(token);
    }
}