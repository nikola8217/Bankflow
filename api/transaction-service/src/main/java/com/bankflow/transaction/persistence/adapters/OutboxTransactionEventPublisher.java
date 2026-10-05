package com.bankflow.transaction.persistence.adapters;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import com.bankflow.transaction.persistence.JsonPayloadSerializer;
import com.bankflow.transaction.persistence.jpa.OutboxJpaEntity;
import com.bankflow.transaction.persistence.jpa.repositories.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OutboxTransactionEventPublisher implements TransactionEventPublisher {

    private final OutboxJpaRepository outboxRepository;
    private final JsonPayloadSerializer serializer;

    @Override
    public void transactionCreated(TransactionCreatedEvent event) {
        OutboxJpaEntity entry = new OutboxJpaEntity();
        entry.setAggregateId(event.transactionId());
        entry.setEventType(TransactionCreatedEvent.class.getSimpleName());
        entry.setPayload(serializer.serialize(event));
        entry.setStatus(OutboxStatus.PENDING);
        entry.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(entry);
    }
}