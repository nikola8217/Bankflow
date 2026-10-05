package com.bankflow.account.application.ports;

import com.bankflow.account.domain.models.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account create(Account account);
    Account update(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findAllByUserId(UUID userId);
}
