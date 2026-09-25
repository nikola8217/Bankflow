package com.bankflow.auth.business.services;

import com.bankflow.auth.business.dtos.LoginUserDto;
import com.bankflow.auth.business.ports.IUserRepository;
import com.bankflow.auth.business.responses.LoginUserResponse;
import com.bankflow.auth.core.entities.User;
import com.bankflow.auth.core.exceptions.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class AuthServiceLoginTest {

    static class InMemoryUserRepository implements IUserRepository {
        private final Map<String, User> store = new HashMap<>();

        @Override
        public User save(User user) {
            store.put(user.getEmail(), user);
            return user;
        }

        @Override
        public Optional<User> findByEmail(String email) {
            return Optional.ofNullable(store.get(email));
        }
    }

    static class CountingPasswordEncoder implements PasswordEncoder {
        private final PasswordEncoder delegate = new BCryptPasswordEncoder(4);
        int matchesCalls = 0;

        @Override
        public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            matchesCalls++;
            return delegate.matches(rawPassword, encodedPassword);
        }
    }

    private InMemoryUserRepository repository;
    private CountingPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
        passwordEncoder = new CountingPasswordEncoder();

        authService = new AuthService(
                repository,
                (userId, email) -> "token-for-" + userId,
                passwordEncoder
        );

        repository.save(new User(UUID.randomUUID(), "ana@test.com",
                passwordEncoder.encode("password123"), "Ana", "Anić"));

        repository.save(User.restore(UUID.randomUUID(), "jelena@test.com",
                passwordEncoder.encode("password123"), "Jelena", "Jelić",
                false, LocalDateTime.now(), LocalDateTime.now()));

        passwordEncoder.matchesCalls = 0;
    }

    @Test
    void correctCredentialsReturnToken() {
        LoginUserResponse response = authService.login(new LoginUserDto("ana@test.com", "password123"));

        assertThat(response.token()).startsWith("token-for-");
    }

    @Test
    void unknownEmailAndWrongPasswordLookExactlyTheSame() {
        Throwable unknownEmail = catchThrowable(() ->
                authService.login(new LoginUserDto("nepostoji@test.com", "password123")));
        Throwable wrongPassword = catchThrowable(() ->
                authService.login(new LoginUserDto("ana@test.com", "pogresna")));

        assertThat(unknownEmail).isInstanceOf(InvalidCredentialsException.class);
        assertThat(wrongPassword).isInstanceOf(InvalidCredentialsException.class);
        assertThat(unknownEmail.getMessage()).isEqualTo(wrongPassword.getMessage());
    }

    @Test
    void passwordIsCheckedEvenWhenEmailDoesNotExist() {
        catchThrowable(() -> authService.login(new LoginUserDto("nepostoji@test.com", "password123")));

        assertThat(passwordEncoder.matchesCalls).isEqualTo(1);
    }

    @Test
    void inactiveUserCannotLoginEvenWithCorrectPassword() {
        Throwable error = catchThrowable(() ->
                authService.login(new LoginUserDto("jelena@test.com", "password123")));

        assertThat(error).isInstanceOf(InvalidCredentialsException.class);
    }
}