package com.bankflow.auth.persistence.jpa;

import com.bankflow.auth.persistence.jpa.UserJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {
    Optional<UserJpaEntity> findByEmail(String email);
}