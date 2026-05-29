package com.bankflow.account.infra.adapters;

import com.bankflow.account.business.ports.IAccountRepository;
import com.bankflow.account.core.entities.Account;
import com.bankflow.account.infra.mappers.AccountMapper;
import com.bankflow.account.infra.repositories.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements IAccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account create(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toNewModel(account)));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Account update(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toExistingModel(account)));
    }
}