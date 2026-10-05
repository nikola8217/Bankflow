package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.LedgerEventPublisher;
import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxLedgerEventPublisher implements LedgerEventPublisher {

    private static final String TRANSACTION_APPROVED_TOPIC = "transaction-approved";
    private static final String TRANSACTION_DECLINED_TOPIC = "transaction-declined";

    private final OutboxWriter outboxWriter;

    @Override
    public void transactionApproved(TransactionApprovedEvent event) {
        outboxWriter.append(new OutboxMessage(
                event.transactionId(),
                TRANSACTION_APPROVED_TOPIC,
                event.transactionId().toString(),
                event));
    }

    @Override
    public void transactionDeclined(TransactionDeclinedEvent event) {
        outboxWriter.append(new OutboxMessage(
                event.transactionId(),
                TRANSACTION_DECLINED_TOPIC,
                event.transactionId().toString(),
                event));
    }
}