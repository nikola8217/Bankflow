package com.bankflow.ledger.infra.consumers;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.services.LedgerService;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
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

    private static final String TOPIC = "transaction-created";
    private static final String DLT = "transaction-created-dlt";
    private static final String POISON = "dfdashdhjk";

    @Autowired
    LedgerService ledgerService;

    @Autowired
    IBalanceRepository balanceRepository;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void poisonMessageGoesToDltAndDoesNotBlockTheConsumer() throws Exception {
        UUID accountId = UUID.randomUUID();
        ledgerService.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {
            producer.send(new ProducerRecord<>(TOPIC, accountId.toString(), POISON)).get();
        }

        TransactionCreatedEvent deposit = new TransactionCreatedEvent(
                UUID.randomUUID(), accountId, UUID.randomUUID(), TransactionType.DEPOSIT,
                new BigDecimal("500.00"), "RSD", null, LocalDateTime.now());
        kafkaTemplate.send(TOPIC, accountId.toString(), deposit).get();

        awaitBalance(accountId, "500.00");

        assertPoisonIsInDlt();
    }

    private void awaitBalance(UUID accountId, String expected) throws InterruptedException {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            BigDecimal amount = balanceRepository.findByAccountId(accountId).orElseThrow().getAmount();
            if (amount.compareTo(new BigDecimal(expected)) == 0) return;
            Thread.sleep(200);
        }
        fail("Balance did not reach " + expected + " within 30s: consumer is probably stuck");
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
                        return;   // našli smo je
                    }
                }
            }
        }
        fail("Poison message not found in " + DLT);
    }
}