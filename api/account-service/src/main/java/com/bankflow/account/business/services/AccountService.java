package com.bankflow.account.business.services;

import com.bankflow.account.business.dtos.CreateAccountDto;
import com.bankflow.account.business.ports.IAccountRepository;
import com.bankflow.account.business.responses.AccountResponse;
import com.bankflow.account.core.entities.Account;
import com.bankflow.account.core.enums.AccountStatus;
import com.bankflow.account.core.exceptions.AccountClosedException;
import com.bankflow.account.core.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {
    private final IAccountRepository accountRepository;

    public AccountService(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountDto dto, UUID userId) {
        Account account = new Account(
                UUID.randomUUID(),
                userId,
                dto.type(),
                AccountStatus.ACTIVE,
                dto.currency()
        );

        return AccountResponse.from(accountRepository.create(account));
    }

    public AccountResponse getAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        return AccountResponse.from(account);
    }

    public List<AccountResponse> getUserAccounts(UUID userId) {
        return accountRepository.findAllByUserId(userId)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse closeAccount(UUID id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() == AccountStatus.CLOSED) throw new AccountClosedException(id);

        account.close();

        return AccountResponse.from(accountRepository.update(account));
    }
}
