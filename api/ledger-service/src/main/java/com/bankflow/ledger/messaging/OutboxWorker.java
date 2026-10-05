package com.bankflow.ledger.messaging;

import com.bankflow.ledger.persistence.JsonPayloadSerializer;
import com.bankflow.ledger.persistence.jpa.OutboxJpaEntity;
import com.bankflow.ledger.persistence.jpa.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OutboxWorker {

    private static final int BATCH_SIZE = 50;
    private static final int RETENTION_DAYS = 7;

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonPayloadSerializer jsonPayloadSerializer;
    private final TransactionTemplate transactionTemplate;

    public OutboxWorker(OutboxJpaRepository outboxRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        JsonPayloadSerializer jsonPayloadSerializer,
                        PlatformTransactionManager transactionManager) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonPayloadSerializer = jsonPayloadSerializer;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        transactionTemplate.executeWithoutResult(status -> {
            for (OutboxJpaEntity entry : outboxRepository.lockNextBatch(BATCH_SIZE)) {
                try {
                    boolean approved = "TransactionApprovedEvent".equals(entry.getEventType());
                    String topic = approved ? "transaction-approved" : "transaction-declined";
                    Object event = approved
                            ? jsonPayloadSerializer.deserialize(entry.getPayload(), TransactionApprovedEvent.class)
                            : jsonPayloadSerializer.deserialize(entry.getPayload(), TransactionDeclinedEvent.class);

                    kafkaTemplate.send(topic, entry.getAggregateId().toString(), event)
                            .get(10, TimeUnit.SECONDS);

                    entry.setStatus(OutboxStatus.PROCESSED);
                    entry.setProcessedAt(LocalDateTime.now());

                    log.info("Outbox entry processed: {}", entry.getId());
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    log.error("Failed to process outbox entry: {}", entry.getId(), e);
                    break;
                }
            }
        });
    }

    @Scheduled(cron = "0 0 * * * *")
    public void cleanup() {
        Integer deleted = transactionTemplate.execute(status ->
                outboxRepository.deleteByStatusAndProcessedAtBefore(
                        OutboxStatus.PROCESSED, LocalDateTime.now().minusDays(RETENTION_DAYS)));
        log.info("Outbox cleanup: deleted {} processed entries", deleted);
    }
}