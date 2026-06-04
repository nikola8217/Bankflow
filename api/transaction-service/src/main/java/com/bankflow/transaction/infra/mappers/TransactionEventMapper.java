package com.bankflow.transaction.infra.mappers;

import com.bankflow.transaction.infra.helpers.JsonSerializer;
import com.bankflow.transaction.infra.models.TransactionEventModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
}