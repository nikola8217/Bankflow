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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Sends pending outbox rows to Kafka, oldest first.
 * Rows are claimed with FOR UPDATE SKIP LOCKED, so several instances can run side by side
 * without sending the same row twice. A row is marked PROCESSED only after Kafka acknowledged it.
 */
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    // Header the Spring Kafka JSON deserializer reads to pick the target class.
    private static final String TYPE_ID_HEADER = "__TypeId__";

    private static final String LOCK_NEXT_BATCH = """
            SELECT id, topic, message_key, event_type, payload
            FROM outbox
            WHERE status = 'PENDING'
            ORDER BY created_at
            LIMIT ?
            FOR UPDATE SKIP LOCKED
            """;

    private static final String MARK_PROCESSED =
            "UPDATE outbox SET status = 'PROCESSED', processed_at = ? WHERE id = ?";

    private static final String DELETE_PROCESSED_BEFORE =
            "DELETE FROM outbox WHERE status = 'PROCESSED' AND processed_at < ?";

    private record PendingRow(UUID id, String topic, String key, String eventType, String payload) {}

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactionTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final int batchSize;
    private final int retentionDays;

    public OutboxRelay(JdbcTemplate jdbc,
                       TransactionTemplate transactionTemplate,
                       KafkaTemplate<String, String> kafkaTemplate,
                       int batchSize,
                       int retentionDays) {
        this.jdbc = jdbc;
        this.transactionTemplate = transactionTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.batchSize = batchSize;
        this.retentionDays = retentionDays;
    }

    @Scheduled(fixedDelayString = "${bankflow.outbox.poll-interval-ms:5000}")
    public void process() {
        transactionTemplate.executeWithoutResult(status -> {
            List<PendingRow> batch = jdbc.query(LOCK_NEXT_BATCH, (rs, i) -> new PendingRow(
                    rs.getObject("id", UUID.class),
                    rs.getString("topic"),
                    rs.getString("message_key"),
                    rs.getString("event_type"),
                    rs.getString("payload")), batchSize);

            for (PendingRow row : batch) {
                try {
                    ProducerRecord<String, String> record = new ProducerRecord<>(row.topic(), row.key(), row.payload());
                    record.headers().add(TYPE_ID_HEADER, row.eventType().getBytes(StandardCharsets.UTF_8));

                    kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);
                    jdbc.update(MARK_PROCESSED, LocalDateTime.now(), row.id());
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                    // Keep order: stop at the first failure, the rest is retried on the next run.
                    log.error("Failed to relay outbox entry {}", row.id(), e);
                    break;
                }
            }
        });
    }

    @Scheduled(cron = "${bankflow.outbox.cleanup-cron:0 0 * * * *}")
    public void cleanup() {
        Integer deleted = transactionTemplate.execute(status ->
                jdbc.update(DELETE_PROCESSED_BEFORE, LocalDateTime.now().minusDays(retentionDays)));
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
}