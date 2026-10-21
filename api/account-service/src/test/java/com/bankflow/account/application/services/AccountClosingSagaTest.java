package com.bankflow.account.application.services;

import com.bankflow.account.domain.enums.AccountStatus;
import com.bankflow.account.domain.enums.AccountType;
import com.bankflow.account.domain.enums.Currency;
import com.bankflow.account.domain.exceptions.AccountClosedException;
import com.bankflow.account.domain.models.Account;
import com.bankflow.shared.events.AccountClosureDecidedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.bankflow.account.application.services.RecordingAccountEventPublisher.closeRequested;
import static com.bankflow.account.application.services.RecordingAccountEventPublisher.closed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The Account side of the closing saga: ACTIVE → CLOSING → CLOSED, or back to ACTIVE when the Ledger says no. */
class AccountClosingSagaTest {

    private final UUID owner = UUID.randomUUID();

    private AccountServiceOwnershipTest.InMemoryAccountRepository repository;
    private RecordingAccountEventPublisher events;
    private AccountService accountService;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        repository = new AccountServiceOwnershipTest.InMemoryAccountRepository();
        events = new RecordingAccountEventPublisher();
        accountService = new AccountService(repository, events);

        Account account = new Account(UUID.randomUUID(), owner, AccountType.CHECKING, AccountStatus.ACTIVE, Currency.RSD);

        repository.create(account);

        accountId = account.getId();
    }

    @Test
    void closingAsksTheLedgerFirst() {
        accountService.closeAccount(accountId, owner);

        assertThat(status()).isEqualTo(AccountStatus.CLOSING);

        assertThat(events.published).containsExactly(closeRequested(accountId));
    }

    @Test
    void askingAgainWhileClosingSendsNoSecondRequest() {
        accountService.closeAccount(accountId, owner);

        accountService.closeAccount(accountId, owner);

        assertThat(events.published).containsExactly(closeRequested(accountId));
    }

    @Test
    void approvedClosureClosesTheAccountAndTellsEveryone() {
        accountService.closeAccount(accountId, owner);

        accountService.applyClosureDecision(AccountClosureDecidedEvent.approved(accountId));

        assertThat(status()).isEqualTo(AccountStatus.CLOSED);

        assertThat(events.published).containsExactly(closeRequested(accountId), closed(accountId));
    }

    @Test
    void rejectedClosureReturnsTheAccountToActive() {
        accountService.closeAccount(accountId, owner);

        accountService.applyClosureDecision(AccountClosureDecidedEvent.rejected(accountId, "Balance is not zero"));

        assertThat(status()).isEqualTo(AccountStatus.ACTIVE);

        assertThat(events.published).containsExactly(closeRequested(accountId));
    }

    @Test
    void redeliveredDecisionIsIgnored() {
        accountService.closeAccount(accountId, owner);

        AccountClosureDecidedEvent approved = AccountClosureDecidedEvent.approved(accountId);

        accountService.applyClosureDecision(approved);

        accountService.applyClosureDecision(approved);

        assertThat(status()).isEqualTo(AccountStatus.CLOSED);

        assertThat(events.published).containsExactly(closeRequested(accountId), closed(accountId));
    }

    @Test
    void closedAccountCannotBeClosedAgain() {
        accountService.closeAccount(accountId, owner);

        accountService.applyClosureDecision(AccountClosureDecidedEvent.approved(accountId));

        assertThatThrownBy(() -> accountService.closeAccount(accountId, owner))
                .isInstanceOf(AccountClosedException.class);
    }

    private AccountStatus status() {
        return repository.findById(accountId).orElseThrow().getStatus();
    }
}