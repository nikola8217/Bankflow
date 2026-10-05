package com.bankflow.auth.persistence.adapters;

import com.bankflow.auth.application.ports.UserRepository;
import com.bankflow.auth.domain.models.User;
import com.bankflow.auth.persistence.jpa.UserJpaRepository;
import com.bankflow.auth.persistence.mappers.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public User save(User user) {
        return mapper.toDomain(jpaRepository.save(mapper.toModel(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toDomain);
    }
}