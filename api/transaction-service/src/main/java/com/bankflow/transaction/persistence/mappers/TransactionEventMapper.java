package com.bankflow.transaction.persistence.mappers;

import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.domain.events.TransactionCompletedEvent;
import com.bankflow.transaction.domain.events.TransactionFailedEvent;
import com.bankflow.transaction.domain.events.TransactionInitiatedEvent;
import com.bankflow.transaction.persistence.JsonPayloadSerializer;
import com.bankflow.transaction.persistence.jpa.TransactionEventJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionEventMapper {

    private final JsonPayloadSerializer jsonPayloadSerializer;

    public TransactionEventJpaEntity toModel(Object event, UUID aggregateId, int version) {
        TransactionEventJpaEntity model = new TransactionEventJpaEntity();
        model.setAggregateId(aggregateId);
        model.setAggregateType("Transaction");
        model.setEventType(event.getClass().getSimpleName());
        model.setVersion(version);
        model.setPayload(jsonPayloadSerializer.serialize(event));
        model.setOccurredAt(LocalDateTime.now());
        return model;
    }

    public TransactionAggregate toAggregate(List<TransactionEventJpaEntity> models) {
        TransactionAggregate aggregate = new TransactionAggregate();

        for (TransactionEventJpaEntity model : models) {
            switch (model.getEventType()) {
                case "TransactionInitiatedEvent" -> aggregate.apply(
                        jsonPayloadSerializer.deserialize(model.getPayload(), TransactionInitiatedEvent.class)
                );
                case "TransactionCompletedEvent" -> aggregate.apply(
                        jsonPayloadSerializer.deserialize(model.getPayload(), TransactionCompletedEvent.class)
                );
                case "TransactionFailedEvent" -> aggregate.apply(
                        jsonPayloadSerializer.deserialize(model.getPayload(), TransactionFailedEvent.class)
                );
            }
        }

        return aggregate;
    }
}