package com.bankflow.outbox;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** The SQL the relay runs against the outbox table. Every method expects to run inside the relay's transaction. */
class OutboxRepository {

    // Any constant works: the lock is local to this service's database, which holds one outbox table.
    private static final long RELAY_LOCK_KEY = 0x6F7574626F78L; // "outbox" in ASCII

    private static final String TRY_RELAY_LOCK = "SELECT pg_try_advisory_xact_lock(?)";

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

    private final JdbcTemplate jdbc;

    OutboxRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** Takes the relay lock until the transaction ends; false if another replica holds it. */
    boolean tryLockRelay() {
        return Boolean.TRUE.equals(jdbc.queryForObject(TRY_RELAY_LOCK, Boolean.class, RELAY_LOCK_KEY));
    }

    List<PendingRow> lockNextBatch(int batchSize) {
        return jdbc.query(LOCK_NEXT_BATCH, OutboxRepository::toPendingRow, batchSize);
    }

    void markProcessed(UUID id) {
        jdbc.update(MARK_PROCESSED, LocalDateTime.now(), id);
    }

    int deleteProcessedBefore(LocalDateTime cutoff) {
        return jdbc.update(DELETE_PROCESSED_BEFORE, cutoff);
    }

    private static PendingRow toPendingRow(ResultSet rs, int rowNum) throws SQLException {
        return new PendingRow(
                rs.getObject("id", UUID.class),
                rs.getString("topic"),
                rs.getString("message_key"),
                rs.getString("event_type"),
                rs.getString("payload"));
    }
}