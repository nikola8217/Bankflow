package com.bankflow.ledger.persistence.mappers;

import com.bankflow.ledger.application.outbox.OutboxEntry;
import com.bankflow.ledger.persistence.JsonPayloadSerializer;
import com.bankflow.ledger.persistence.jpa.OutboxJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final JsonPayloadSerializer jsonPayloadSerializer;

    public OutboxJpaEntity toModel(OutboxEntry entry) {
        OutboxJpaEntity model = new OutboxJpaEntity();
        model.setAggregateId(entry.getAggregateId());
        model.setEventType(entry.getEventType());
        model.setPayload(jsonPayloadSerializer.serialize(entry.getPayload()));
        model.setStatus(entry.getStatus());
        model.setCreatedAt(entry.getCreatedAt());
        return model;
    }

    public OutboxEntry toDomain(OutboxJpaEntity model) {
        return OutboxEntry.builder()
                .id(model.getId())
                .aggregateId(model.getAggregateId())
                .eventType(model.getEventType())
                .payload(model.getPayload())
                .status(model.getStatus())
                .createdAt(model.getCreatedAt())
                .processedAt(model.getProcessedAt())
                .build();
    }
}