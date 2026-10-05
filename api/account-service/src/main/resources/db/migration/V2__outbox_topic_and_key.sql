ALTER TABLE outbox ADD COLUMN topic VARCHAR(255);
ALTER TABLE outbox ADD COLUMN message_key VARCHAR(255);

UPDATE outbox
SET topic       = 'account-created',
    message_key = aggregate_id::text,
    event_type  = 'com.bankflow.shared.events.AccountCreatedEvent'
WHERE topic IS NULL;

ALTER TABLE outbox ALTER COLUMN topic SET NOT NULL;
ALTER TABLE outbox ALTER COLUMN message_key SET NOT NULL;