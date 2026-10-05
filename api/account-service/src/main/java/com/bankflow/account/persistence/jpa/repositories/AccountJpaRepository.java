package com.bankflow.account.persistence.jpa.repositories;

import com.bankflow.account.persistence.jpa.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    List<AccountJpaEntity> findAllByUserId(UUID userId);
}