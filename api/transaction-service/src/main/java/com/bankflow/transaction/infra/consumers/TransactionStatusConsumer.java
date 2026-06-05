package com.bankflow.transaction.infra.consumers;

import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import com.bankflow.transaction.business.CommandBus;
import com.bankflow.transaction.business.commands.CompleteTransactionCommand;
import com.bankflow.transaction.business.commands.FailTransactionCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionStatusConsumer {

    private final CommandBus commandBus;

    @KafkaListener(topics = "transaction-approved", groupId = "transaction-service")
    public void handleApproved(TransactionApprovedEvent event) {
        log.info("Received TransactionApprovedEvent for: {}", event.transactionId());
        commandBus.send(new CompleteTransactionCommand(event.transactionId()));
    }

    @KafkaListener(topics = "transaction-declined", groupId = "transaction-service")
    public void handleDeclined(TransactionDeclinedEvent event) {
        log.info("Received TransactionDeclinedEvent for: {}", event.transactionId());
        commandBus.send(new FailTransactionCommand(event.transactionId(), event.reason()));
    }
}