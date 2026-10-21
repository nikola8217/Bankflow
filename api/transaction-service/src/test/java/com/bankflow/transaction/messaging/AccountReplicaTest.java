package com.bankflow.transaction.messaging;

import com.bankflow.shared.events.AccountClosedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.Topics;
import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.application.bus.CommandBus;
import com.bankflow.transaction.application.commands.MarkAccountClosedCommand;
import com.bankflow.transaction.application.commands.RecordAccountCommand;
import com.bankflow.transaction.application.ports.AccountDirectory;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

class AccountReplicaTest extends AbstractIntegrationTest {

    @Autowired
    AccountDirectory accounts;

    @Autowired
    CommandBus commandBus;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    JsonMapper jsonMapper;

    @Test
    void accountCreatedEventMakesTheAccountKnown() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        publish(Topics.ACCOUNT_CREATED, accountId, new AccountCreatedEvent(accountId, userId, "EUR"));

        assertThat(awaitAccount(accountId, account -> true))
                .contains(new AccountSnapshot(accountId, userId, "EUR", "ACTIVE"));
    }

    @Test
    void accountClosedEventMarksTheAccountClosed() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        commandBus.send(new RecordAccountCommand(accountId, userId, "RSD"));

        publish(Topics.ACCOUNT_CLOSED, accountId, new AccountClosedEvent(accountId, userId, "RSD"));

        assertThat(awaitAccount(accountId, account -> "CLOSED".equals(account.status())))
                .contains(new AccountSnapshot(accountId, userId, "RSD", "CLOSED"));
    }

    /** The topics are not ordered against each other, so the closure can arrive before the creation. */
    @Test
    void closureThatArrivesFirstIsNotUndoneByALateCreation() {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        commandBus.send(new MarkAccountClosedCommand(accountId, userId, "RSD"));
        commandBus.send(new RecordAccountCommand(accountId, userId, "RSD"));

        assertThat(accounts.findAccount(accountId))
                .contains(new AccountSnapshot(accountId, userId, "RSD", "CLOSED"));
    }

    @Test
    void redeliveredEventIsHarmless() {
        UUID accountId = UUID.randomUUID();

        RecordAccountCommand command = new RecordAccountCommand(accountId, UUID.randomUUID(), "RSD");

        commandBus.send(command);
        commandBus.send(command);

        Integer rows = jdbc.queryForObject(
                "SELECT COUNT(*) FROM account_replica WHERE id = ?", Integer.class, accountId);

        assertThat(rows).isEqualTo(1);
    }

    @Test
    void unknownAccountIsNotFound() {
        assertThat(accounts.findAccount(UUID.randomUUID())).isEmpty();
    }

    /** Sends the event the way the Account service's outbox relay does: JSON with the type in a header. */
    private void publish(String topic, UUID accountId, Object event) throws Exception {
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA.getBootstrapServers(),
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class))) {

            ProducerRecord<String, String> record = new ProducerRecord<>(
                    topic, accountId.toString(), jsonMapper.writeValueAsString(event));

            record.headers().add("__TypeId__", event.getClass().getName().getBytes(StandardCharsets.UTF_8));

            producer.send(record).get(10, TimeUnit.SECONDS);
        }
    }

    /** The consumer runs asynchronously, so poll until the account matches (or give up after 15 s). */
    private Optional<AccountSnapshot> awaitAccount(UUID accountId, Predicate<AccountSnapshot> condition)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + 15_000;

        Optional<AccountSnapshot> account = accounts.findAccount(accountId);

        while (account.filter(condition).isEmpty() && System.currentTimeMillis() < deadline) {
            Thread.sleep(200);
            account = accounts.findAccount(accountId);
        }

        return account;
    }
}