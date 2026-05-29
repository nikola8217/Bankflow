package com.bankflow.account.business.ports;

import com.bankflow.account.core.entities.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAccountRepository {
    Account create(Account account);
    Account update(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findAllByUserId(UUID userId);
}
