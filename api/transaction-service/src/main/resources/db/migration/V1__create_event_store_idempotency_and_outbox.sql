CREATE TABLE transaction_events (
    id             UUID         PRIMARY KEY,
    aggregate_id   UUID         NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    event_type     VARCHAR(255) NOT NULL,
    version        INTEGER      NOT NULL,
    payload        JSONB        NOT NULL,
    occurred_at    TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_transaction_events_aggregate_version UNIQUE (aggregate_id, version)
);

CREATE TABLE idempotency_keys (
    id                  UUID         PRIMARY KEY,
    user_id             UUID         NOT NULL,
    idempotency_key     VARCHAR(255) NOT NULL,
    request_fingerprint VARCHAR(500) NOT NULL,
    transaction_id      UUID         NOT NULL,
    created_at          TIMESTAMP(6) NOT NULL,
    CONSTRAINT uk_idempotency_user_key UNIQUE (user_id, idempotency_key)
);

CREATE TABLE outbox (
    id           UUID         PRIMARY KEY,
    aggregate_id UUID         NOT NULL,
    event_type   VARCHAR(255) NOT NULL,
    payload      JSONB        NOT NULL,
    status       VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP(6) NOT NULL,
    processed_at TIMESTAMP(6)
);

CREATE INDEX idx_outbox_pending ON outbox (created_at) WHERE status = 'PENDING';