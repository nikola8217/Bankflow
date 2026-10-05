ALTER TABLE outbox ADD COLUMN topic VARCHAR(255);
ALTER TABLE outbox ADD COLUMN message_key VARCHAR(255);

UPDATE outbox
SET topic       = 'transaction-created',
    message_key = payload ->> 'accountId',
    event_type  = 'com.bankflow.shared.events.TransactionCreatedEvent'
WHERE topic IS NULL;

ALTER TABLE outbox ALTER COLUMN topic SET NOT NULL;
ALTER TABLE outbox ALTER COLUMN message_key SET NOT NULL;