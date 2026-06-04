package com.bankflow.ledger.infra.consumers;

import com.bankflow.ledger.business.projections.BalanceProjection;
import com.bankflow.ledger.business.projections.TransactionHistoryProjection;
import com.bankflow.shared.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final BalanceProjection balanceProjection;
    private final TransactionHistoryProjection historyProjection;

    @KafkaListener(topics = "transaction-created", groupId = "ledger-service")
    public void consume(TransactionCreatedEvent event) {
        log.info("Received TransactionCreatedEvent for transaction: {}", event.transactionId());

        balanceProjection.handle(event);
        historyProjection.handle(event);

        log.info("Projections updated for transaction: {}", event.transactionId());
    }
}