CREATE TABLE balances (
    id         UUID           PRIMARY KEY,
    user_id    UUID,
    account_id UUID           NOT NULL,
    amount     NUMERIC(38, 2) NOT NULL,
    currency   VARCHAR(255)   NOT NULL,
    updated_at TIMESTAMP(6)   NOT NULL,
    CONSTRAINT uk_balances_account_id UNIQUE (account_id)
);

CREATE TABLE transaction_history (
    id                UUID           PRIMARY KEY,
    transaction_id    UUID           NOT NULL,
    account_id        UUID           NOT NULL,
    user_id           UUID           NOT NULL,
    type              VARCHAR(255)   NOT NULL,
    amount            NUMERIC(38, 2) NOT NULL,
    currency          VARCHAR(255)   NOT NULL,
    target_account_id UUID,
    status            VARCHAR(255)   NOT NULL,
    created_at        TIMESTAMP(6)   NOT NULL,
    failure_reason    VARCHAR(255),
    CONSTRAINT uk_transaction_history_transaction_id UNIQUE (transaction_id)
);

CREATE INDEX idx_transaction_history_account ON transaction_history (account_id, created_at DESC);
CREATE INDEX idx_transaction_history_target ON transaction_history (target_account_id, created_at DESC);

CREATE TABLE processed_events (
    transaction_id VARCHAR(255) PRIMARY KEY,
    processed_at   TIMESTAMP(6) NOT NULL
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