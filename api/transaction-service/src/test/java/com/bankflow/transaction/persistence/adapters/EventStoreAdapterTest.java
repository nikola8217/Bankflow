package com.bankflow.transaction.persistence.adapters;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.domain.exceptions.ConcurrencyConflictException;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventStoreAdapterTest extends AbstractIntegrationTest {

    @Autowired
    EventStore eventStore;

    @Test
    void staleWriterIsRejectedAndTheFirstDecisionWins() {
        UUID transactionId = initiatedTransaction();

        TransactionAggregate first = eventStore.load(transactionId).orElseThrow();
        TransactionAggregate second = eventStore.load(transactionId).orElseThrow();

        first.complete();
        eventStore.save(first);

        second.fail("declined");
        assertThatThrownBy(() -> eventStore.save(second))
                .isInstanceOf(ConcurrencyConflictException.class);

        TransactionAggregate stored = eventStore.load(transactionId).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(stored.getVersion()).isEqualTo(2);
    }

    @Test
    void writerWithCurrentVersionCanAppend() {
        UUID transactionId = initiatedTransaction();

        TransactionAggregate aggregate = eventStore.load(transactionId).orElseThrow();
        aggregate.complete();
        eventStore.save(aggregate);

        assertThat(eventStore.load(transactionId).orElseThrow().getStatus())
                .isEqualTo(TransactionStatus.COMPLETED);
    }

    private UUID initiatedTransaction() {
        UUID transactionId = UUID.randomUUID();
        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(transactionId, UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.DEPOSIT, new BigDecimal("100.00"), "RSD", null);
        eventStore.save(aggregate);
        return transactionId;
    }
}