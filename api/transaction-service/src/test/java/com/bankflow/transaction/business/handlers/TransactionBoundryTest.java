package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.exceptions.ServiceUnavailableException;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.ITransactionRunner;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionBoundaryTest {

    static class RecordingTransactionRunner implements ITransactionRunner {
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

    private final IIdempotencyRepository idempotency = new InMemoryIdempotencyRepository();

    private final IEventStore eventStore = new IEventStore() {
        public void save(TransactionAggregate aggregate) { aggregate.pullDomainEvents(); }
        public Optional<TransactionAggregate> load(UUID id) { return Optional.empty(); }
    };

    private WithdrawCommandHandler handlerWith(IAccountClient accountClient) {
        return new WithdrawCommandHandler(accountClient, outbox::add, idempotency, runner, eventStore);
    }

    private WithdrawCommand withdrawal() {
        return new WithdrawCommand(new AccountTransactionDto(
                accountId, new BigDecimal("100.00"), null, userId, UUID.randomUUID().toString()));
    }

    @Test
    void accountServiceIsCalledOutsideTheDatabaseTransaction() {
        IAccountClient accountClient = id -> {
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
        IAccountClient accountClient = id -> {
            throw new ServiceUnavailableException("Account service");
        };

        assertThatThrownBy(() -> handlerWith(accountClient).handle(withdrawal()))
                .isInstanceOf(ServiceUnavailableException.class);

        assertThat(runner.transactions).isZero();
        assertThat(outbox).isEmpty();
    }
}