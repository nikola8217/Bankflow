package com.bankflow.balance.infra.repositories;

import com.bankflow.balance.infra.models.BalanceModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BalanceJpaRepository extends JpaRepository<BalanceModel, UUID> {
    Optional<BalanceModel> findByAccountId(UUID accountId);
}