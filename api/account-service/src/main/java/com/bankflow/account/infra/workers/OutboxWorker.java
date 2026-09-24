package com.bankflow.account.infra.workers;

import com.bankflow.account.infra.models.OutboxModel;
import com.bankflow.account.infra.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboxWorker.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String TOPIC = "account-created";

    private final OutboxJpaRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OutboxWorker(OutboxJpaRepository outboxRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    public void process() {
        List<OutboxModel> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        for (OutboxModel entry : pending) {
            try {
                AccountCreatedEvent event = MAPPER.readValue(entry.getPayload(), AccountCreatedEvent.class);

                kafkaTemplate.send(TOPIC, event.accountId().toString(), event)
                        .get(10, TimeUnit.SECONDS);

                entry.setStatus(OutboxStatus.PROCESSED);
                entry.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(entry);

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