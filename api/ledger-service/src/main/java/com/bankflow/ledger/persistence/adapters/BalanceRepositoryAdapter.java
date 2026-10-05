package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.BalanceRepository;
import com.bankflow.ledger.domain.models.Balance;
import com.bankflow.ledger.persistence.mappers.BalanceMapper;
import com.bankflow.ledger.persistence.jpa.repositories.BalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BalanceRepositoryAdapter implements BalanceRepository {

    private final BalanceJpaRepository jpaRepository;
    private final BalanceMapper mapper;

    @Override
    public void save(Balance balance) {
        jpaRepository.findByAccountId(balance.getAccountId())
                .ifPresentOrElse(
                        existing -> {
                            existing.setAmount(balance.getAmount());
                            existing.setUpdatedAt(balance.getUpdatedAt());
                            jpaRepository.save(existing);
                        },
                        () -> jpaRepository.save(mapper.toModel(balance))
                );
    }

    @Override
    public Optional<Balance> findByAccountId(UUID accountId) {
        return jpaRepository.findByAccountId(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public void ensureExists(UUID accountId, String currency) {
        jpaRepository.insertIfMissing(accountId, currency);
    }

    @Override
    public Optional<Balance> findByAccountIdForUpdate(UUID accountId) {
        return jpaRepository.findByAccountIdForUpdate(accountId)
                .map(mapper::toDomain);
    }

    @Override
    public void registerAccount(UUID accountId, UUID userId, String currency) {
        jpaRepository.upsertAccount(accountId, userId, currency);
    }
}