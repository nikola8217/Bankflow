package com.bankflow.auth.infra.repositories;

import com.bankflow.auth.infra.models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserModel, UUID> {
    Optional<UserModel> findByEmail(String email);
}