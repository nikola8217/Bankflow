package com.bankflow.transaction.messaging;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class OutboxConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    TransactionEventPublisher eventPublisher;

    @Autowired
    OutboxWorker outboxWorker;

    @Test
    void parallelWorkersPublishEachEntryExactlyOnce() throws Exception {
        UUID accountId = UUID.randomUUID();
        int entries = 50;
        for (int i = 0; i < entries; i++) {
            eventPublisher.transactionCreated(eventFor(accountId));
        }

        int workers = 4;
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        for (int w = 0; w < workers; w++) {
            results.add(pool.submit(() -> {
                startGate.await();
                for (int round = 0; round < 5; round++) {
                    outboxWorker.process();
                }
                return null;
            }));
        }

        startGate.countDown();
        for (Future<?> result : results) {
            result.get(120, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(countPublishedFor(accountId)).isEqualTo(entries);
    }

    private TransactionCreatedEvent eventFor(UUID accountId) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(), accountId, UUID.randomUUID(), TransactionType.WITHDRAWAL,
                new BigDecimal("1.00"), "RSD", null, LocalDateTime.now());
    }

    private int countPublishedFor(UUID accountId) {
        int count = 0;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "outbox-count-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {

            consumer.subscribe(List.of("transaction-created"));
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (accountId.toString().equals(record.key())) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}