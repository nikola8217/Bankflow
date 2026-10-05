package com.bankflow.transaction.application.handlers;

import com.bankflow.transaction.application.commands.DepositCommand;
import com.bankflow.transaction.application.commands.TransferCommand;
import com.bankflow.transaction.application.commands.WithdrawCommand;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.dtos.Transfer;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.ports.TransactionRunner;
import com.bankflow.transaction.application.dtos.TransactionCreatedResponse;
import com.bankflow.transaction.application.dtos.TransferResponse;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.domain.exceptions.IdempotencyKeyReusedException;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyKeyTest {

    private final UUID ana = UUID.randomUUID();
    private final UUID jelena = UUID.randomUUID();
    private final UUID anasAccount = UUID.randomUUID();
    private final UUID anasSecondAccount = UUID.randomUUID();
    private final UUID jelenasAccount = UUID.randomUUID();

    private final List<TransactionCreatedEvent> outbox = new ArrayList<>();
    private final InMemoryIdempotencyRepositoryTest idempotency = new InMemoryIdempotencyRepositoryTest();

    private DepositCommandHandler depositHandler;
    private WithdrawCommandHandler withdrawHandler;
    private TransferCommandHandler transferHandler;

    @BeforeEach
    void setUp() {
        TransactionAuthorizationTest.StubAccountClient accountClient = new TransactionAuthorizationTest.StubAccountClient();
        accountClient.accounts.put(anasAccount, new AccountSnapshot(anasAccount, ana, "CHECKING", "RSD", "ACTIVE"));
        accountClient.accounts.put(anasSecondAccount, new AccountSnapshot(anasSecondAccount, ana, "SAVINGS", "RSD", "ACTIVE"));
        accountClient.accounts.put(jelenasAccount, new AccountSnapshot(jelenasAccount, jelena, "CHECKING", "RSD", "ACTIVE"));

        EventStore eventStore = new EventStore() {
            public void save(TransactionAggregate aggregate) { aggregate.pullDomainEvents(); }
            public Optional<TransactionAggregate> load(UUID id) { return Optional.empty(); }
        };
        TransactionRunner runner = Runnable::run;
        IdempotencyGuard guard = new IdempotencyGuard(idempotency, runner);

        depositHandler = new DepositCommandHandler(accountClient, outbox::add, guard, eventStore);
        withdrawHandler = new WithdrawCommandHandler(accountClient, outbox::add, guard, eventStore);
        transferHandler = new TransferCommandHandler(accountClient, outbox::add, guard, eventStore);
    }

    @Test
    void retryWithSameKeyReturnsTheOriginalTransaction() {
        TransactionCreatedResponse first = withdrawHandler.handle(withdraw(ana, anasAccount, "100.00", "key-1"));
        TransactionCreatedResponse retry = withdrawHandler.handle(withdraw(ana, anasAccount, "100.00", "key-1"));

        assertThat(retry.id()).isEqualTo(first.id());
        assertThat(outbox).hasSize(1);
    }

    @Test
    void sameAmountWrittenDifferentlyIsStillTheSameRequest() {
        TransactionCreatedResponse first = depositHandler.handle(deposit(ana, anasAccount, "100.00", "key-1"));
        TransactionCreatedResponse retry = depositHandler.handle(deposit(ana, anasAccount, "100", "key-1"));

        assertThat(retry.id()).isEqualTo(first.id());
        assertThat(outbox).hasSize(1);
    }

    @Test
    void transferRetryReturnsTheSameResponse() {
        TransferResponse first = transferHandler.handle(transfer(ana, anasAccount, jelenasAccount, "key-1"));
        TransferResponse retry = transferHandler.handle(transfer(ana, anasAccount, jelenasAccount, "key-1"));

        assertThat(retry).isEqualTo(first);
        assertThat(outbox).hasSize(1);
    }

    @Test
    void differentUsersCanUseTheSameKey() {
        TransactionCreatedResponse anas = depositHandler.handle(deposit(ana, anasAccount, "100.00", "shared-key"));
        TransactionCreatedResponse jelenas = depositHandler.handle(deposit(jelena, jelenasAccount, "100.00", "shared-key"));

        assertThat(jelenas.id()).isNotEqualTo(anas.id());
        assertThat(outbox).hasSize(2);
    }

    @Test
    void sameKeyWithDifferentAmountIsRejected() {
        withdrawHandler.handle(withdraw(ana, anasAccount, "100.00", "key-1"));

        assertThatThrownBy(() -> withdrawHandler.handle(withdraw(ana, anasAccount, "999.00", "key-1")))
                .isInstanceOf(IdempotencyKeyReusedException.class);

        assertThat(outbox).hasSize(1);
    }

    @Test
    void sameKeyForDifferentOperationIsRejected() {
        depositHandler.handle(deposit(ana, anasAccount, "100.00", "key-1"));

        assertThatThrownBy(() -> withdrawHandler.handle(withdraw(ana, anasAccount, "100.00", "key-1")))
                .isInstanceOf(IdempotencyKeyReusedException.class);

        assertThat(outbox).hasSize(1);
    }

    @Test
    void sameKeyForDifferentAccountIsRejected() {
        depositHandler.handle(deposit(ana, anasAccount, "100.00", "key-1"));

        assertThatThrownBy(() -> depositHandler.handle(deposit(ana, anasSecondAccount, "100.00", "key-1")))
                .isInstanceOf(IdempotencyKeyReusedException.class);

        assertThat(outbox).hasSize(1);
    }

    private DepositCommand deposit(UUID userId, UUID accountId, String amount, String key) {
        return new DepositCommand(new AccountTransaction(accountId, new BigDecimal(amount), null, userId, key));
    }

    private WithdrawCommand withdraw(UUID userId, UUID accountId, String amount, String key) {
        return new WithdrawCommand(new AccountTransaction(accountId, new BigDecimal(amount), null, userId, key));
    }

    private TransferCommand transfer(UUID userId, UUID from, UUID to, String key) {
        return new TransferCommand(new Transfer(from, to, new BigDecimal("100.00"), null, userId, key));
    }
}