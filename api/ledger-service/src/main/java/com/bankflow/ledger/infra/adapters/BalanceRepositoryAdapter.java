package com.bankflow.ledger.infra.adapters;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.infra.mappers.BalanceMapper;
import com.bankflow.ledger.infra.repositories.BalanceJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BalanceRepositoryAdapter implements IBalanceRepository {

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
}