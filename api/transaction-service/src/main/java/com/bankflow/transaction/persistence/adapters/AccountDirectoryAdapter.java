package com.bankflow.transaction.persistence.adapters;

import com.bankflow.transaction.application.ports.AccountDirectory;
import com.bankflow.transaction.application.ports.AccountDirectoryWriter;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import com.bankflow.transaction.persistence.jpa.repositories.AccountReplicaJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountDirectoryAdapter implements AccountDirectory, AccountDirectoryWriter {

    private final AccountReplicaJpaRepository repository;

    @Override
    public Optional<AccountSnapshot> findAccount(UUID accountId) {
        return repository.findById(accountId)
                .map(entity -> new AccountSnapshot(
                        entity.getId(), entity.getUserId(), entity.getCurrency(), entity.getStatus()));
    }

    @Override
    public void addIfAbsent(AccountSnapshot account) {
        repository.insertIfAbsent(account.id(), account.userId(), account.currency(), account.status());
    }

    @Override
    public void markClosed(AccountSnapshot account) {
        repository.upsertClosed(account.id(), account.userId(), account.currency());
    }
}