package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.LedgerEventPublisher;
import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import com.bankflow.shared.events.Topics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxLedgerEventPublisher implements LedgerEventPublisher {

    private final OutboxWriter outboxWriter;

    @Override
    public void transactionApproved(TransactionApprovedEvent event) {
        outboxWriter.append(new OutboxMessage(
                event.transactionId(),
                Topics.TRANSACTION_APPROVED,
                event.transactionId().toString(),
                event));
    }

    @Override
    public void transactionDeclined(TransactionDeclinedEvent event) {
        outboxWriter.append(new OutboxMessage(
                event.transactionId(),
                Topics.TRANSACTION_DECLINED,
                event.transactionId().toString(),
                event));
    }
}