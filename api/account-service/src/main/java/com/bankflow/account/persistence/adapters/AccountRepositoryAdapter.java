package com.bankflow.account.persistence.adapters;

import com.bankflow.account.application.ports.AccountRepository;
import com.bankflow.account.domain.models.Account;
import com.bankflow.account.persistence.mappers.AccountMapper;
import com.bankflow.account.persistence.jpa.repositories.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository jpaRepository;
    private final AccountMapper mapper;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository, AccountMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Account create(Account account) {
        return mapper.toDomain(jpaRepository.save(mapper.toModel(account)));
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
        return mapper.toDomain(jpaRepository.save(mapper.toModel(account)));
    }
}