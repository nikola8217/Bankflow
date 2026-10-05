package com.bankflow.outbox;

import org.springframework.jdbc.core.JdbcTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Stores an event in the outbox table. Call it inside the business transaction:
 * the event is saved if and only if the business change is committed.
 */
public class OutboxWriter {

    private static final String INSERT = """
            INSERT INTO outbox (id, aggregate_id, event_type, topic, message_key, payload, status, created_at)
            VALUES (?, ?, ?, ?, ?, CAST(? AS jsonb), 'PENDING', ?)
            """;

    private final JdbcTemplate jdbc;
    private final JsonMapper jsonMapper;

    public OutboxWriter(JdbcTemplate jdbc, JsonMapper jsonMapper) {
        this.jdbc = jdbc;
        this.jsonMapper = jsonMapper;
    }

    public void append(OutboxMessage message) {
        jdbc.update(INSERT,
                UUID.randomUUID(),
                message.aggregateId(),
                message.event().getClass().getName(),
                message.topic(),
                message.key(),
                jsonMapper.writeValueAsString(message.event()),
                LocalDateTime.now());
    }
}