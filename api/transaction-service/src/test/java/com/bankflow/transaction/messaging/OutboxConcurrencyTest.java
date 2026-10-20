package com.bankflow.transaction.messaging;

import com.bankflow.outbox.OutboxRelay;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
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
    OutboxRelay outboxRelay;

    @Autowired
    DataSource dataSource;

    @Autowired
    PlatformTransactionManager transactionManager;

    @Test
    void parallelWorkersPublishEachEntryExactlyOnce() throws Exception {
        UUID accountId = UUID.randomUUID();
        int entries = 50;

        for (int i = 0; i < entries; i++) {
            eventPublisher.transactionCreated(eventFor(UUID.randomUUID(), accountId));
        }

        runInParallel(List.of(outboxRelay, outboxRelay, outboxRelay, outboxRelay), 5);

        assertThat(publishedFor(accountId)).hasSize(entries);
    }

    /**
     * Several relays with small batches, all on events of one account (one Kafka key, so one partition).
     * If two relays published different batches at the same time, a newer event could reach Kafka before
     * an older one, and the Ledger would see a withdrawal before the deposit that funds it.
     */
    @Test
    void parallelWorkersKeepTheOrderOfOneAccountsEvents() throws Exception {
        UUID accountId = UUID.randomUUID();
        List<String> writtenOrder = new ArrayList<>();

        for (int i = 0; i < 60; i++) {
            UUID transactionId = UUID.randomUUID();

            writtenOrder.add(transactionId.toString());

            eventPublisher.transactionCreated(eventFor(transactionId, accountId));
        }

        List<OutboxRelay> relays = List.of(smallBatchRelay(), smallBatchRelay(), smallBatchRelay(), smallBatchRelay());

        try {
            runInParallel(relays, 20);
        } finally {
            for (OutboxRelay relay : relays) {
                relay.close();
            }
        }
        // Relays that found the lock taken skipped their round; finish whatever is left, one relay at a time.
        for (int i = 0; i < 5; i++) {
            outboxRelay.process();
        }

        List<String> publishedOrder = publishedFor(accountId).stream()
                .map(value -> writtenOrder.stream().filter(value::contains).findFirst().orElseThrow())
                .toList();

        assertThat(publishedOrder).containsExactlyElementsOf(writtenOrder);
    }

    private TransactionCreatedEvent eventFor(UUID transactionId, UUID accountId) {
        return new TransactionCreatedEvent(
                transactionId, accountId, UUID.randomUUID(), TransactionType.WITHDRAWAL,
                new BigDecimal("1.00"), "RSD", null, LocalDateTime.now());
    }

    private OutboxRelay smallBatchRelay() {
        KafkaTemplate<String, String> kafkaTemplate = new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
                Map.of(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers()),
                new StringSerializer(),
                new StringSerializer()));

        return new OutboxRelay(new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager),
                kafkaTemplate, 3, 7);
    }

    private void runInParallel(List<OutboxRelay> relays, int rounds) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(relays.size());
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        for (OutboxRelay relay : relays) {
            results.add(pool.submit(() -> {
                startGate.await();
                for (int round = 0; round < rounds; round++) {
                    relay.process();
                }
                return null;
            }));
        }
        startGate.countDown();

        for (Future<?> result : results) {
            result.get(120, TimeUnit.SECONDS);
        }
        pool.shutdown();
    }

    /** Values of the account's events on transaction-created, in the order Kafka holds them. */
    private List<String> publishedFor(UUID accountId) {
        List<String> values = new ArrayList<>();

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "outbox-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {

            consumer.subscribe(List.of("transaction-created"));

            long deadline = System.currentTimeMillis() + 10_000;

            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (accountId.toString().equals(record.key())) {
                        values.add(record.value());
                    }
                }
            }
        }

        return values;
    }
}