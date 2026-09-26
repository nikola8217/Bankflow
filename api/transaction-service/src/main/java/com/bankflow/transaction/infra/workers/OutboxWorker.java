package com.bankflow.transaction.infra.workers;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.infra.helpers.JsonSerializer;
import com.bankflow.transaction.infra.models.OutboxModel;
import com.bankflow.transaction.infra.repositories.OutboxJpaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OutboxWorker {

    private static final String TOPIC = "transaction-created";
    private static final int BATCH_SIZE = 50;
    private static final int RETENTION_DAYS = 7;

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonSerializer jsonSerializer;
    private final TransactionTemplate transactionTemplate;

    public OutboxWorker(OutboxJpaRepository outboxRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        JsonSerializer jsonSerializer,
                        PlatformTransactionManager transactionManager) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonSerializer = jsonSerializer;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        transactionTemplate.executeWithoutResult(status -> {
            List<OutboxModel> batch = outboxRepository.lockNextBatch(BATCH_SIZE);

            for (OutboxModel entry : batch) {
                try {
                    TransactionCreatedEvent event =
                            jsonSerializer.deserialize(entry.getPayload(), TransactionCreatedEvent.class);

                    kafkaTemplate.send(TOPIC, event.accountId().toString(), event)
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