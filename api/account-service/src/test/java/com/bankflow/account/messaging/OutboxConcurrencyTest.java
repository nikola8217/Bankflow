package com.bankflow.account.messaging;

import com.bankflow.account.AbstractIntegrationTest;
import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxRelay;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.AccountCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
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
    OutboxWriter outboxWriter;

    @Autowired
    OutboxRelay outboxRelay;

    @Test
    void parallelWorkersPublishEachEntryExactlyOnce() throws Exception {
        UUID userId = UUID.randomUUID();
        int entries = 50;
        for (int i = 0; i < entries; i++) {
            UUID accountId = UUID.randomUUID();
            outboxWriter.append(new OutboxMessage(
                    accountId,
                    "account-created",
                    accountId.toString(),
                    new AccountCreatedEvent(accountId, userId, "RSD")));
        }

        runWorkersInParallel(4);

        assertThat(countPublished("account-created", userId.toString())).isEqualTo(entries);
    }

    private void runWorkersInParallel(int workers) throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();
        for (int w = 0; w < workers; w++) {
            results.add(pool.submit(() -> {
                startGate.await();
                for (int round = 0; round < 5; round++) {
                    outboxRelay.process();
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

    private int countPublished(String topic, String marker) {
        int count = 0;
        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "outbox-count-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class))) {

            consumer.subscribe(List.of(topic));
            long deadline = System.currentTimeMillis() + 10_000;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    if (record.value().contains(marker)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}