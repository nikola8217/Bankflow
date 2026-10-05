package com.bankflow.outbox;

import java.util.Objects;
import java.util.UUID;

/**
 * An integration event plus where it has to go.
 *
 * @param aggregateId the business object the event is about (e.g. the transaction id)
 * @param topic       Kafka topic
 * @param key         Kafka message key; events with the same key keep their order
 * @param event       the event itself, serialized to JSON
 */
public record OutboxMessage(UUID aggregateId, String topic, String key, Object event) {

    public OutboxMessage {
        Objects.requireNonNull(aggregateId, "aggregateId");
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(event, "event");
    }
}