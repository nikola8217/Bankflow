package com.bankflow.account.business.services;

import com.bankflow.account.business.dtos.CreateAccountDto;
import com.bankflow.account.business.ports.IAccountEventPublisher;
import com.bankflow.account.business.ports.IAccountRepository;
import com.bankflow.account.business.responses.AccountResponse;
import com.bankflow.account.core.entities.Account;
import com.bankflow.account.core.enums.AccountStatus;
import com.bankflow.account.core.exceptions.AccountClosedException;
import com.bankflow.account.core.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final IAccountRepository accountRepository;
    private final IAccountEventPublisher eventPublisher;

    public AccountService(IAccountRepository accountRepository, IAccountEventPublisher eventPublisher) {

        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AccountResponse createAccount(CreateAccountDto dto) {
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
