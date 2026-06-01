package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.IEventPublisher;
import com.bankflow.transaction.core.events.TransactionCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventPublisherAdapter implements IEventPublisher {

    private static final String TOPIC = "transaction-created";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public EventPublisherAdapter(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(TransactionCreatedEvent event) {
        kafkaTemplate.send(TOPIC, event.transactionId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.err.println("Failed to send event: " + ex.getMessage());
                    }
                });
    }
}