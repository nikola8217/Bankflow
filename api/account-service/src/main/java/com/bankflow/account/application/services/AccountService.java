package com.bankflow.account.application.services;

import com.bankflow.account.application.dtos.CreateAccount;
import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.application.ports.AccountRepository;
import com.bankflow.account.application.dtos.AccountResponse;
import com.bankflow.account.domain.models.Account;
import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.exceptions.AccountClosedException;
import com.bankflow.account.domain.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountEventPublisher eventPublisher;

    public AccountService(AccountRepository accountRepository, AccountEventPublisher eventPublisher) {

        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccount dto) {
        Account account = new Account(
                UUID.randomUUID(),
                dto.userId(),
                dto.type(),
                AccountStatus.ACTIVE,
                dto.currency()
        );

        Account saved = accountRepository.create(account);
        eventPublisher.accountCreated(saved);

        return AccountResponse.from(saved);
    }

    public List<AccountResponse> getUserAccounts(UUID userId) {
        return accountRepository.findAllByUserId(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse getAccount(UUID id, UUID currentUserId) {
        return AccountResponse.from(findOwned(id, currentUserId));
    }

    public AccountResponse getAccountInternal(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
        return AccountResponse.from(account);
    }

    public AccountResponse closeAccount(UUID id, UUID currentUserId) {
        Account account = findOwned(id, currentUserId);

        if (account.getStatus() == AccountStatus.CLOSED) throw new AccountClosedException(id);

        account.close();

        return AccountResponse.from(accountRepository.update(account));
    }

    private Account findOwned(UUID id, UUID currentUserId) {
        return accountRepository.findById(id)
                .filter(account -> account.getUserId().equals(currentUserId))
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
