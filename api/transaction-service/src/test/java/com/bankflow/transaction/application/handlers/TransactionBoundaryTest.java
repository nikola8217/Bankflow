package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.exceptions.ServiceUnavailableException;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;
import com.bankflow.transaction.application.commands.WithdrawCommand;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.ports.AccountClient;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.ports.IdempotencyRepository;
import com.bankflow.transaction.application.ports.TransactionRunner;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.application.outbox.OutboxEntry;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionBoundaryTest {

    static class RecordingTransactionRunner implements TransactionRunner {
        boolean inside = false;
        int transactions = 0;

        @Override
        public void inTransaction(Runnable work) {
            inside = true;
            transactions++;
            try {
                work.run();
            } finally {
                inside = false;
            }
        }
    }

    private final UUID userId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();
    private final RecordingTransactionRunner runner = new RecordingTransactionRunner();
    private final List<OutboxEntry> outbox = new ArrayList<>();

    private final IdempotencyRepository idempotency = new InMemoryIdempotencyRepositoryTest();

    private final EventStore eventStore = new EventStore() {
        public void save(TransactionAggregate aggregate) { aggregate.pullDomainEvents(); }
        public Optional<TransactionAggregate> load(UUID id) { return Optional.empty(); }
    };

    private WithdrawCommandHandler handlerWith(AccountClient accountClient) {
        return new WithdrawCommandHandler(accountClient, outbox::add,
                new IdempotencyGuard(idempotency, runner), eventStore);
    }

    private WithdrawCommand withdrawal() {
        return new WithdrawCommand(new AccountTransaction(
                accountId, new BigDecimal("100.00"), null, userId, UUID.randomUUID().toString()));
    }

    @Test
    void accountServiceIsCalledOutsideTheDatabaseTransaction() {
        AccountClient accountClient = id -> {
            if (runner.inside) {
                throw new AssertionError("HTTP call made while holding a DB transaction!");
            }
            return new AccountSnapshot(id, userId, "CHECKING", "RSD", "ACTIVE");
        };

        handlerWith(accountClient).handle(withdrawal());

        assertThat(runner.transactions).isEqualTo(1);
        assertThat(outbox).hasSize(1);
    }

    @Test
    void noDatabaseWorkWhenAccountServiceIsDown() {
        AccountClient accountClient = id -> {
            throw new ServiceUnavailableException("Account service");
        };

        assertThatThrownBy(() -> handlerWith(accountClient).handle(withdrawal()))
                .isInstanceOf(ServiceUnavailableException.class);

        assertThat(runner.transactions).isZero();
        assertThat(outbox).isEmpty();
    }
}