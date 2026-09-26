package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.CompleteTransactionCommand;
import com.bankflow.transaction.business.commands.FailTransactionCommand;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.exceptions.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionStatusHandlersTest {

    static class InMemoryEventStore implements IEventStore {
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

    private InMemoryEventStore eventStore;
    private CompleteTransactionHandler completeHandler;
    private FailTransactionHandler failHandler;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        eventStore = new InMemoryEventStore();
        completeHandler = new CompleteTransactionHandler(eventStore);
        failHandler = new FailTransactionHandler(eventStore);

        transactionId = UUID.randomUUID();
        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(transactionId, UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.WITHDRAWAL, new BigDecimal("100.00"), "RSD", null);
        eventStore.save(aggregate);
    }

    private TransactionAggregate current() {
        return eventStore.load(transactionId).orElseThrow();
    }

    @Test
    void duplicateApprovedIsIgnored() {
        completeHandler.handle(new CompleteTransactionCommand(transactionId));

        completeHandler.handle(new CompleteTransactionCommand(transactionId));

        assertThat(current().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(current().getVersion()).isEqualTo(2);
    }

    @Test
    void duplicateDeclinedIsIgnored() {
        failHandler.handle(new FailTransactionCommand(transactionId, "Insufficient funds"));

        failHandler.handle(new FailTransactionCommand(transactionId, "Insufficient funds"));

        assertThat(current().getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(current().getVersion()).isEqualTo(2);
    }

    @Test
    void approvedAfterDeclinedIsStillAnError() {
        failHandler.handle(new FailTransactionCommand(transactionId, "Insufficient funds"));

        assertThatThrownBy(() -> completeHandler.handle(new CompleteTransactionCommand(transactionId)))
                .isInstanceOf(TransactionException.class);

        assertThat(current().getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void declinedAfterApprovedIsStillAnError() {
        completeHandler.handle(new CompleteTransactionCommand(transactionId));

        assertThatThrownBy(() -> failHandler.handle(new FailTransactionCommand(transactionId, "late")))
                .isInstanceOf(TransactionException.class);

        assertThat(current().getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }
}