package com.bankflow.outbox;

import java.util.UUID;

/** An outbox row waiting to be sent to Kafka. */
record PendingRow(UUID id, String topic, String key, String eventType, String payload) {}