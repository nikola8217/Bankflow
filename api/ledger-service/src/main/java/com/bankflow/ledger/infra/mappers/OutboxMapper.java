package com.bankflow.ledger.infra.mappers;

import com.bankflow.ledger.core.entities.OutboxEntry;
import com.bankflow.ledger.infra.helpers.JsonSerializer;
import com.bankflow.ledger.infra.models.OutboxModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxMapper {

    private final JsonSerializer jsonSerializer;

    public OutboxModel toModel(OutboxEntry entry) {
        OutboxModel model = new OutboxModel();
        model.setAggregateId(entry.getAggregateId());
        model.setEventType(entry.getEventType());
        model.setPayload(jsonSerializer.serialize(entry.getPayload()));
        model.setStatus(entry.getStatus());
        model.setCreatedAt(entry.getCreatedAt());
        return model;
    }

    public OutboxEntry toDomain(OutboxModel model) {
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