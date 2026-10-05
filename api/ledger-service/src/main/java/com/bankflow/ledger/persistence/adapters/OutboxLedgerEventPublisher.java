package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.LedgerEventPublisher;
import com.bankflow.ledger.persistence.JsonPayloadSerializer;
import com.bankflow.ledger.persistence.jpa.OutboxJpaEntity;
import com.bankflow.ledger.persistence.jpa.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxLedgerEventPublisher implements LedgerEventPublisher {

    private final OutboxJpaRepository outboxRepository;
    private final JsonPayloadSerializer serializer;

    @Override
    public void transactionApproved(TransactionApprovedEvent event) {
        store(event.transactionId(), TransactionApprovedEvent.class.getSimpleName(), event);
    }

    @Override
    public void transactionDeclined(TransactionDeclinedEvent event) {
        store(event.transactionId(), TransactionDeclinedEvent.class.getSimpleName(), event);
    }

    private void store(UUID transactionId, String eventType, Object event) {
        OutboxJpaEntity entry = new OutboxJpaEntity();
        entry.setAggregateId(transactionId);
        entry.setEventType(eventType);
        entry.setPayload(serializer.serialize(event));
        entry.setStatus(OutboxStatus.PENDING);
        entry.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(entry);
    }
}