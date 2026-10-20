package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;
import com.bankflow.transaction.application.commands.DepositCommand;
import com.bankflow.transaction.application.commands.TransferCommand;
import com.bankflow.transaction.application.commands.WithdrawCommand;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.dtos.Transfer;
import com.bankflow.transaction.application.ports.*;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.domain.exceptions.AccountNotFoundException;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionAuthorizationTest  {

    static class StubAccountDirectory implements AccountDirectory {
        final Map<UUID, AccountSnapshot> accounts = new HashMap<>();

        @Override
        public Optional<AccountSnapshot> findAccount(UUID accountId) {
            return Optional.ofNullable(accounts.get(accountId));
        }
    }

    static class InMemoryEventStore implements EventStore {
        final Map<UUID, TransactionAggregate> store = new HashMap<>();

        @Override
        public void save(TransactionAggregate aggregate) {
            aggregate.pullDomainEvents();
            store.put(aggregate.getTransactionId(), aggregate);
        }

        @Override
        public Optional<TransactionAggregate> load(UUID transactionId) {
            return Optional.ofNullable(store.get(transactionId));
        }
    }

    private final List<TransactionCreatedEvent> outbox = new ArrayList<>();

    private final TransactionEventPublisher eventPublisher = outbox::add;

    private final UUID ana = UUID.randomUUID();
    private final UUID jelena = UUID.randomUUID();
    private final UUID anasAccount = UUID.randomUUID();
    private final UUID jelenasAccount = UUID.randomUUID();

    private StubAccountDirectory accountClient;
    private DepositCommandHandler depositHandler;
    private WithdrawCommandHandler withdrawHandler;
    private TransferCommandHandler transferHandler;

    @BeforeEach
    void setUp() {
        accountClient = new StubAccountDirectory();

        accountClient.accounts.put(anasAccount,
                new AccountSnapshot(anasAccount, ana,"RSD", "ACTIVE"));

        accountClient.accounts.put(jelenasAccount,
                new AccountSnapshot(jelenasAccount, jelena,"RSD", "ACTIVE"));

        InMemoryEventStore eventStore = new InMemoryEventStore();

        InMemoryIdempotencyRepositoryTest idempotency = new InMemoryIdempotencyRepositoryTest();

        TransactionRunner runner = Runnable::run;

        IdempotencyGuard guard = new IdempotencyGuard(idempotency, runner);

        depositHandler = new DepositCommandHandler(accountClient, eventPublisher, guard, eventStore);
        withdrawHandler = new WithdrawCommandHandler(accountClient, eventPublisher, guard, eventStore);
        transferHandler = new TransferCommandHandler(accountClient, eventPublisher, guard, eventStore);
    }

    @Test
    void ownerCanWithdrawFromOwnAccount() {
        withdrawHandler.handle(new WithdrawCommand(withdraw(anasAccount, ana)));

        assertThat(outbox).hasSize(1);
    }

    @Test
    void otherUserCannotWithdrawFromSomeoneElsesAccount() {
        assertThatThrownBy(() -> withdrawHandler.handle(new WithdrawCommand(withdraw(anasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(outbox).isEmpty();
    }

    @Test
    void otherUserCannotDepositToSomeoneElsesAccount() {
        assertThatThrownBy(() -> depositHandler.handle(new DepositCommand(withdraw(anasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(outbox).isEmpty();
    }

    @Test
    void transferToSomeoneElsesAccountIsAllowed() {
        transferHandler.handle(new TransferCommand(transfer(anasAccount, jelenasAccount, ana)));

        assertThat(outbox).hasSize(1);
    }

    @Test
    void cannotTransferFromSomeoneElsesAccount() {
        assertThatThrownBy(() -> transferHandler.handle(new TransferCommand(transfer(anasAccount, jelenasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(outbox).isEmpty();
    }

    private AccountTransaction withdraw(UUID accountId, UUID userId) {
        return new AccountTransaction(accountId, new BigDecimal("100.00"), null, userId,
                UUID.randomUUID().toString());
    }

    private Transfer transfer(UUID from, UUID to, UUID userId) {
        return new Transfer(from, to, new BigDecimal("100.00"), null, userId,
                UUID.randomUUID().toString());
    }
}