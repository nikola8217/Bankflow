package com.bankflow.ledger.infra.consumers;

import com.bankflow.ledger.business.services.LedgerService;
import com.bankflow.shared.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final LedgerService ledgerService;

    @KafkaListener(topics = "transaction-created", groupId = "ledger-service")
    public void consume(TransactionCreatedEvent event) {
        log.info("Received TransactionCreatedEvent for transaction: {}", event.transactionId());
        ledgerService.process(event);
    }
}