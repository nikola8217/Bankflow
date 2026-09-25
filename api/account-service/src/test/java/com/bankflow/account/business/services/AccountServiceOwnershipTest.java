package com.bankflow.account.business.services;

import com.bankflow.account.business.ports.IAccountRepository;
import com.bankflow.account.business.responses.AccountResponse;
import com.bankflow.account.core.entities.Account;
import com.bankflow.account.core.enums.AccountStatus;
import com.bankflow.account.core.enums.AccountType;
import com.bankflow.account.core.enums.Currency;
import com.bankflow.account.core.exceptions.AccountNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceOwnershipTest {

    static class InMemoryAccountRepository implements IAccountRepository {
        private final Map<UUID, Account> store = new HashMap<>();

        @Override
        public Account create(Account account) {
            store.put(account.getId(), account);
            return account;
        }

        @Override
        public Account update(Account account) {
            store.put(account.getId(), account);
            return account;
        }

        @Override
        public Optional<Account> findById(UUID id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public List<Account> findAllByUserId(UUID userId) {
            return store.values().stream()
                    .filter(a -> a.getUserId().equals(userId))
                    .toList();
        }
    }

    private final UUID ana = UUID.randomUUID();
    private final UUID jelena = UUID.randomUUID();

    private InMemoryAccountRepository repository;
    private AccountService accountService;
    private UUID anasAccountId;

    @BeforeEach
    void setUp() {
        repository = new InMemoryAccountRepository();

        accountService = new AccountService(repository, account -> {});

        Account anasAccount = new Account(
                UUID.randomUUID(), ana, AccountType.CHECKING, AccountStatus.ACTIVE, Currency.RSD);
        repository.create(anasAccount);
        anasAccountId = anasAccount.getId();
    }

    @Test
    void ownerCanSeeOwnAccount() {
        AccountResponse response = accountService.getAccount(anasAccountId, ana);

        assertThat(response.id()).isEqualTo(anasAccountId);
    }

    @Test
    void otherUserCannotSeeAccount() {
        assertThatThrownBy(() -> accountService.getAccount(anasAccountId, jelena))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void otherUserCannotCloseAccount() {
        assertThatThrownBy(() -> accountService.closeAccount(anasAccountId, jelena))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(repository.findById(anasAccountId).orElseThrow().getStatus())
                .isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void ownerCanCloseAccount() {
        AccountResponse response = accountService.closeAccount(anasAccountId, ana);

        assertThat(response.status()).isEqualTo(AccountStatus.CLOSED);
    }

    @Test
    void internalLookupIgnoresOwnership() {
        AccountResponse response = accountService.getAccountInternal(anasAccountId);

        assertThat(response.userId()).isEqualTo(ana);
    }
}