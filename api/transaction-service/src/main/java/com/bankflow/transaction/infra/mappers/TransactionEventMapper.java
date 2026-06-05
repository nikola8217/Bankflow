package com.bankflow.transaction.infra.mappers;

import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.events.TransactionCompletedEvent;
import com.bankflow.transaction.core.events.TransactionFailedEvent;
import com.bankflow.transaction.core.events.TransactionInitiatedEvent;
import com.bankflow.transaction.infra.helpers.JsonSerializer;
import com.bankflow.transaction.infra.models.TransactionEventModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionEventMapper {

    private final JsonSerializer jsonSerializer;

    public TransactionEventModel toModel(Object event, UUID aggregateId, int version) {
        TransactionEventModel model = new TransactionEventModel();
        model.setAggregateId(aggregateId);
        model.setAggregateType("Transaction");
        model.setEventType(event.getClass().getSimpleName());
        model.setVersion(version);
        model.setPayload(jsonSerializer.serialize(event));
        model.setOccurredAt(LocalDateTime.now());
        return model;
    }

    public TransactionAggregate toAggregate(List<TransactionEventModel> models) {
        TransactionAggregate aggregate = new TransactionAggregate();

        for (TransactionEventModel model : models) {
            switch (model.getEventType()) {
                case "TransactionInitiatedEvent" -> aggregate.apply(
                        jsonSerializer.deserialize(model.getPayload(), TransactionInitiatedEvent.class)
                );
                case "TransactionCompletedEvent" -> aggregate.apply(
                        jsonSerializer.deserialize(model.getPayload(), TransactionCompletedEvent.class)
                );
                case "TransactionFailedEvent" -> aggregate.apply(
                        jsonSerializer.deserialize(model.getPayload(), TransactionFailedEvent.class)
                );
            }
        }

        return aggregate;
    }
}