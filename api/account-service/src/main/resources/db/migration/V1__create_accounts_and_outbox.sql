CREATE TABLE accounts (
    id         UUID         PRIMARY KEY,
    user_id    UUID         NOT NULL,
    type       VARCHAR(255) NOT NULL,
    currency   VARCHAR(255) NOT NULL,
    status     VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL
);

CREATE INDEX idx_accounts_user_id ON accounts (user_id);

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