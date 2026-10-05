package com.bankflow.transaction.messaging;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.application.ports.OutboxRepository;
import com.bankflow.transaction.application.outbox.OutboxEntry;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

class OutboxWorkerTest extends AbstractIntegrationTest {

    @Autowired
    OutboxRepository outboxRepository;

    @Test
    void publishesTransactionCreatedKeyedByAccountId() {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        outboxRepository.save(OutboxEntry.builder()
                .aggregateId(transactionId)
                .eventType("TransactionCreatedEvent")
                .payload(new TransactionCreatedEvent(
                        transactionId, accountId, UUID.randomUUID(), TransactionType.WITHDRAWAL,
                        new BigDecimal("100.00"), "RSD", null, LocalDateTime.now()))
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        ConsumerRecord<String, String> record = awaitRecordFor(transactionId);

        assertThat(record.key()).isEqualTo(accountId.toString());
    }

    private ConsumerRecord<String, String> awaitRecordFor(UUID transactionId) {
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "outbox-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {

            consumer.subscribe(List.of("transaction-created"));
            long deadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (record.value().contains(transactionId.toString())) {
                        return record;
                    }
                }
            }
        }
        fail("TransactionCreatedEvent for " + transactionId + " was not published within 30s");
        return null;
    }
}