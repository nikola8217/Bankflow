package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.IEventPublisher;
import com.bankflow.transaction.core.events.TransactionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherAdapter implements IEventPublisher {

    private static final String TOPIC = "transaction-created";

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    public EventPublisherAdapter(KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.transactionId().toString(), event);
    }
}