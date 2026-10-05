package com.bankflow.account.messaging;

import com.bankflow.account.persistence.jpa.OutboxJpaEntity;
import com.bankflow.account.persistence.jpa.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.AccountCreatedEvent;
import tools.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);
    private static final String TOPIC = "account-created";
    private static final int BATCH_SIZE = 50;
    private static final int RETENTION_DAYS = 7;

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TransactionTemplate transactionTemplate;
    private final JsonMapper jsonMapper;

    public OutboxWorker(OutboxJpaRepository outboxRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        PlatformTransactionManager transactionManager,
                        JsonMapper jsonMapper) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        transactionTemplate.executeWithoutResult(status -> {
            for (OutboxJpaEntity entry : outboxRepository.lockNextBatch(BATCH_SIZE)) {
                try {
                    AccountCreatedEvent event = jsonMapper.readValue(entry.getPayload(), AccountCreatedEvent.class);

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