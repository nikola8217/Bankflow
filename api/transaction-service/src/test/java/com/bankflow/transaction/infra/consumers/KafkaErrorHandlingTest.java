package com.bankflow.transaction.infra.consumers;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.fail;

class KafkaErrorHandlingTest extends AbstractIntegrationTest {

    private static final String TOPIC = "transaction-approved";
    private static final String DLT = "transaction-approved-dlt";
    private static final String POISON = "ovo nije json";

    @Autowired
    IEventStore eventStore;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void poisonMessageGoesToDltAndDoesNotBlockTheConsumer() throws Exception {
        UUID transactionId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(transactionId, accountId, UUID.randomUUID(),
                TransactionType.WITHDRAWAL, new BigDecimal("100.00"), "RSD", null);
        eventStore.save(aggregate);

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
            producer.send(new ProducerRecord<>(TOPIC, transactionId.toString(), POISON)).get();
        }

        kafkaTemplate.send(TOPIC, transactionId.toString(), new TransactionApprovedEvent(
                transactionId, accountId, UUID.randomUUID(), TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"), "RSD", null, LocalDateTime.now())).get();

        awaitStatus(transactionId, TransactionStatus.COMPLETED);

        assertPoisonIsInDlt();
    }

    private void awaitStatus(UUID transactionId, TransactionStatus expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            TransactionStatus status = eventStore.load(transactionId).orElseThrow().getStatus();
            if (status == expected) return;
            Thread.sleep(200);
        }
        fail("Transaction did not become " + expected + " within 30s: consumer is probably stuck");
    }

    private void assertPoisonIsInDlt() {
        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "dlt-test-" + UUID.randomUUID(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class))) {

            consumer.subscribe(List.of(DLT));
            long deadline = System.currentTimeMillis() + 30_000;
            while (System.currentTimeMillis() < deadline) {
                for (ConsumerRecord<String, byte[]> record : consumer.poll(Duration.ofMillis(500))) {
                    if (POISON.equals(new String(record.value(), StandardCharsets.UTF_8))) {
                        return;
                    }
                }
            }
        }
        fail("Poison message not found in " + DLT);
    }
}