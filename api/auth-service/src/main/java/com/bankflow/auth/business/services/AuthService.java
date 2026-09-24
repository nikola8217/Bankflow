package com.bankflow.auth.business.services;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.business.dtos.RegisterUserDto;
import com.bankflow.auth.business.ports.ITokenService;
import com.bankflow.auth.business.ports.IUserRepository;
import com.bankflow.auth.business.responses.LoginUserResponse;
import com.bankflow.auth.business.responses.RegisterUserResponse;
import com.bankflow.auth.core.entities.User;
import com.bankflow.auth.core.exceptions.InvalidCredentialsException;
import com.bankflow.auth.core.exceptions.UserAlreadyExistsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final IUserRepository userRepository;
    private final ITokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final String dummyHash;

    public AuthService(IUserRepository userRepository, ITokenService tokenService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("timing-attack-protection");
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

    public LoginUserResponse login(LoginUserDto dto) {
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