package com.bankflow.transaction.core.aggregates;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.core.events.TransactionCompletedEvent;
import com.bankflow.transaction.core.events.TransactionFailedEvent;
import com.bankflow.transaction.core.events.TransactionInitiatedEvent;
import com.bankflow.transaction.core.exceptions.TransactionException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionAggregateTest {

    private TransactionAggregate initiatedWithdrawal() {
        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(
                UUID.randomUUID(),     // transactionId
                UUID.randomUUID(),     // accountId
                UUID.randomUUID(),     // userId
                TransactionType.WITHDRAWAL,
                new BigDecimal("300.00"),
                "RSD",
                null
        );
        return aggregate;
    }

    @Test
    void initiateStartsInPendingAndRecordsEvent() {
        TransactionAggregate aggregate = initiatedWithdrawal();

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(aggregate.getVersion()).isEqualTo(1);

        List<Object> events = aggregate.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(TransactionInitiatedEvent.class);
    }

    @Test
    void completeMovesPendingToCompleted() {
        TransactionAggregate aggregate = initiatedWithdrawal();
        aggregate.pullDomainEvents();

        aggregate.complete();

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(aggregate.getVersion()).isEqualTo(2);

        List<Object> events = aggregate.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(TransactionCompletedEvent.class);
    }

    @Test
    void failMovesPendingToFailed() {
        TransactionAggregate aggregate = initiatedWithdrawal();
        aggregate.pullDomainEvents();

        aggregate.fail("Insufficient funds");

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(aggregate.getVersion()).isEqualTo(2);

        List<Object> events = aggregate.pullDomainEvents();
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(TransactionFailedEvent.class);
    }

    @Test
    void cannotCompleteTwice() {
        TransactionAggregate aggregate = initiatedWithdrawal();
        aggregate.complete();

        assertThatThrownBy(aggregate::complete)
                .isInstanceOf(TransactionException.class);

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(aggregate.getVersion()).isEqualTo(2);
    }

    @Test
    void cannotCompleteAfterFailure() {
        TransactionAggregate aggregate = initiatedWithdrawal();
        aggregate.fail("Insufficient funds");

        assertThatThrownBy(aggregate::complete)
                .isInstanceOf(TransactionException.class);

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void cannotFailAfterCompletion() {
        TransactionAggregate aggregate = initiatedWithdrawal();
        aggregate.complete();

        assertThatThrownBy(() -> aggregate.fail("too late"))
                .isInstanceOf(TransactionException.class);

        assertThat(aggregate.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void cannotInitiateTwice() {
        TransactionAggregate aggregate = initiatedWithdrawal();

        assertThatThrownBy(() -> aggregate.initiate(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TransactionType.DEPOSIT, new BigDecimal("1.00"), "RSD", null))
                .isInstanceOf(TransactionException.class);

        assertThat(aggregate.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(aggregate.getVersion()).isEqualTo(1);
    }

    @Test
    void pullDomainEventsEmptiesTheList() {
        TransactionAggregate aggregate = initiatedWithdrawal();

        aggregate.pullDomainEvents();

        assertThat(aggregate.pullDomainEvents()).isEmpty();
    }

    @Test
    void replayingEventsRebuildsTheSameState() {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionAggregate rebuilt = new TransactionAggregate();
        rebuilt.apply(new TransactionInitiatedEvent(
                transactionId, accountId, UUID.randomUUID(),
                TransactionType.WITHDRAWAL, new BigDecimal("300.00"), "RSD", null));
        rebuilt.apply(new TransactionCompletedEvent(transactionId, LocalDateTime.now()));

        assertThat(rebuilt.getTransactionId()).isEqualTo(transactionId);
        assertThat(rebuilt.getAccountId()).isEqualTo(accountId);
        assertThat(rebuilt.getAmount()).isEqualByComparingTo("300.00");
        assertThat(rebuilt.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(rebuilt.getVersion()).isEqualTo(2);

        assertThat(rebuilt.pullDomainEvents()).isEmpty();
    }
}