package com.bankflow.transaction.persistence.adapters;

import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.Topics;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxTransactionEventPublisher implements TransactionEventPublisher {

    private final OutboxWriter outboxWriter;

    @Override
    public void transactionCreated(TransactionCreatedEvent event) {
        outboxWriter.append(new OutboxMessage(
                event.transactionId(),
                Topics.TRANSACTION_CREATED,
                event.accountId().toString(),
                event));
    }
}