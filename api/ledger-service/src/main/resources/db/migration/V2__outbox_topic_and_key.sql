ALTER TABLE outbox ADD COLUMN topic VARCHAR(255);
ALTER TABLE outbox ADD COLUMN message_key VARCHAR(255);

UPDATE outbox
SET topic       = 'transaction-approved',
    message_key = aggregate_id::text,
    event_type  = 'com.bankflow.shared.events.TransactionApprovedEvent'
WHERE topic IS NULL AND event_type = 'TransactionApprovedEvent';

UPDATE outbox
SET topic       = 'transaction-declined',
    message_key = aggregate_id::text,
    event_type  = 'com.bankflow.shared.events.TransactionDeclinedEvent'
WHERE topic IS NULL AND event_type = 'TransactionDeclinedEvent';

ALTER TABLE outbox ALTER COLUMN topic SET NOT NULL;
ALTER TABLE outbox ALTER COLUMN message_key SET NOT NULL;