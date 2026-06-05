package com.bankflow.ledger.infra.workers;

import com.bankflow.ledger.core.entities.OutboxEntry;
import com.bankflow.ledger.infra.adapters.OutboxRepositoryAdapter;
import com.bankflow.ledger.infra.helpers.JsonSerializer;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxWorker {

    private final OutboxRepositoryAdapter outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonSerializer jsonSerializer;

    @Scheduled(fixedDelay = 5000)
    public void process() {
        List<OutboxEntry> pending = outboxRepository.findPending();

        for (OutboxEntry entry : pending) {
            try {
                boolean isApproved = entry.getEventType().equals("TransactionApprovedEvent");

                String topic = isApproved ? "transaction-approved" : "transaction-declined";

                Object event = isApproved
                        ? jsonSerializer.deserialize(entry.getPayload().toString(), TransactionApprovedEvent.class)
                        : jsonSerializer.deserialize(entry.getPayload().toString(), TransactionDeclinedEvent.class);

                kafkaTemplate.send(topic, entry.getAggregateId().toString(), event);
                outboxRepository.markAsProcessed(entry.getId());
                log.info("Outbox entry processed: {}", entry.getId());
            } catch (Exception e) {
                log.error("Failed to process outbox entry: {}", entry.getId(), e);
            }
        }
    }
}