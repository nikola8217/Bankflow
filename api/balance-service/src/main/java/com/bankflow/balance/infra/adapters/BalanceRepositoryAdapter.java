package com.bankflow.balance.infra.adapters;

import com.bankflow.balance.business.ports.IBalanceRepository;
import com.bankflow.balance.core.entities.Balance;
import com.bankflow.balance.infra.mappers.BalanceMapper;
import com.bankflow.balance.infra.repositories.BalanceJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BalanceRepositoryAdapter implements IBalanceRepository {

    private final BalanceJpaRepository jpaRepository;
    private final BalanceMapper mapper;

    public BalanceRepositoryAdapter(BalanceJpaRepository jpaRepository, BalanceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Balance create(Balance balance) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewModel(balance)));
    }

    @Override
    public Balance update(Balance balance) {
        return mapper.toDomain(jpaRepository.save(mapper.toExistingModel(balance)));
    }

    @Override
    public Optional<Balance> findByAccountId(UUID accountId) {
        return jpaRepository.findByAccountId(accountId).map(mapper::toDomain);
    }
}