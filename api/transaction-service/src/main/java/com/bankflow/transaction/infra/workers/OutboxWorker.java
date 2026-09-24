package com.bankflow.transaction.infra.workers;

import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.transaction.infra.adapters.OutboxRepositoryAdapter;
import com.bankflow.transaction.infra.helpers.JsonSerializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
                TransactionCreatedEvent event = jsonSerializer.deserialize(
                        entry.getPayload().toString(),
                        TransactionCreatedEvent.class
                );

                kafkaTemplate.send("transaction-created", entry.getAggregateId().toString(), event)
                        .get(10, TimeUnit.SECONDS);

                outboxRepository.markAsProcessed(entry.getId());
                log.info("Outbox entry processed: {}", entry.getId());
            } catch (Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                log.error("Failed to process outbox entry: {}", entry.getId(), e);
                break;
            }
        }
    }
}