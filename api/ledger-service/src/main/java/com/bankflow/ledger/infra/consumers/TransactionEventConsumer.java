package com.bankflow.ledger.infra.consumers;

import com.bankflow.ledger.business.projections.BalanceProjection;
import com.bankflow.ledger.business.projections.TransactionHistoryProjection;
import com.bankflow.ledger.infra.adapters.ProcessedEventRepositoryAdapter;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
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
    private final ProcessedEventRepositoryAdapter processedEventAdapter;

    @KafkaListener(topics = "transaction-created", groupId = "ledger-service")
    public void consume(TransactionCreatedEvent event) {
        String key = event.transactionId() + "_CREATED";

        if (processedEventAdapter.exists(key)) {
            log.warn("Event already processed: {}", key);
            return;
        }

        log.info("Received TransactionCreatedEvent for transaction: {}", key);

        balanceProjection.handle(event);

        if (event.type() == TransactionType.DEPOSIT) {
            historyProjection.handle(event);
        }

        processedEventAdapter.save(key);
    }

    @KafkaListener(topics = "transaction-approved", groupId = "ledger-service")
    public void handleTransactionApproved(TransactionApprovedEvent event) {
        String key = event.transactionId() + "_APPROVED";

        if (processedEventAdapter.exists(key)) {
            log.warn("Event already processed: {}", key);
            return;
        }

        log.info("Received TransactionApprovedEvent for: {}", key);

        balanceProjection.handleApproved(event);
        historyProjection.handleApproved(event);

        processedEventAdapter.save(key);
    }

    @KafkaListener(topics = "transaction-declined", groupId = "ledger-service")
    public void handleTransactionDeclined(TransactionDeclinedEvent event) {
        String key = event.transactionId() + "_DECLINED";

        if (processedEventAdapter.exists(key)) {
            log.warn("Event already processed: {}", key);
            return;
        }

        log.info("Received TransactionDeclinedEvent for: {}", key);

        historyProjection.handleDeclined(event);

        processedEventAdapter.save(key);
    }
}