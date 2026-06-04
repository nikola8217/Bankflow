package com.bankflow.ledger.infra.repositories;

import com.bankflow.ledger.infra.models.BalanceModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BalanceJpaRepository extends JpaRepository<BalanceModel, UUID> {
    Optional<BalanceModel> findByAccountId(UUID accountId);
}