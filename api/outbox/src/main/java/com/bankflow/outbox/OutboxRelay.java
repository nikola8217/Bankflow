package com.bankflow.outbox;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Sends pending outbox rows to Kafka, oldest first, and marks a row PROCESSED only after Kafka acknowledged it.
 * Every replica runs the relay, but only the one holding an advisory lock publishes. Two relays sending
 * different batches at once could put an account's newer event on Kafka before an older one. The other
 * replicas are hot standbys: the lock is released when the active relay's transaction ends, even if it dies.
 */
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    // Header the Spring Kafka JSON deserializer reads to pick the target class.
    private static final String TYPE_ID_HEADER = "__TypeId__";
    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final OutboxRepository outbox;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final int batchSize;
    private final int retentionDays;

    public OutboxRelay(JdbcTemplate jdbc,
                       TransactionTemplate transactionTemplate,
                       KafkaTemplate<String, String> kafkaTemplate,
                       int batchSize,
                       int retentionDays) {
        this.outbox = new OutboxRepository(jdbc);
        this.transactionTemplate = transactionTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
        this.retentionDays = retentionDays;
    }

    @Scheduled(fixedDelayString = "${bankflow.outbox.poll-interval-ms:5000}")
    public void process() {
        transactionTemplate.executeWithoutResult(status -> {
            if (!outbox.tryLockRelay()) {
                return; // another replica is publishing
            }
            for (PendingRow row : outbox.lockNextBatch(batchSize)) {
                if (!publish(row)) {
                    break; // keep order: this row and the rest are retried on the next run
                }

                outbox.markProcessed(row.id());
            }
        });
    }

    @Scheduled(cron = "${bankflow.outbox.cleanup-cron:0 0 * * * *}")
    public void cleanup() {
        Integer deleted = transactionTemplate.execute(status ->
                outbox.deleteProcessedBefore(LocalDateTime.now().minusDays(retentionDays)));

        log.info("Outbox cleanup: deleted {} processed entries", deleted);
    }

    /**
     * Spring calls a public close() on @Bean objects at shutdown (inferred destroy method),
     * so the relay's own Kafka producer is flushed and closed together with the context.
     */
    public void close() throws Exception {
        if (kafkaTemplate.getProducerFactory() instanceof DisposableBean factory) {
            factory.destroy();
        }
    }

    /** Sends one row and waits for Kafka's acknowledgement; false if it did not come. */
    private boolean publish(PendingRow row) {
        ProducerRecord<String, String> record = new ProducerRecord<>(row.topic(), row.key(), row.payload());

        record.headers().add(TYPE_ID_HEADER, row.eventType().getBytes(StandardCharsets.UTF_8));

        try {
            kafkaTemplate.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error("Interrupted while relaying outbox entry {}", row.id(), e);

            return false;
        } catch (Exception e) {
            log.error("Failed to relay outbox entry {}", row.id(), e);

            return false;
        }
    }
}