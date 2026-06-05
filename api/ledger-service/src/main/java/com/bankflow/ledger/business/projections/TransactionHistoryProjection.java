package com.bankflow.ledger.business.projections;

import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionHistoryProjection {

    private final ITransactionHistoryRepository historyRepository;

    public void handle(TransactionCreatedEvent event) {
        TransactionHistory history = new TransactionHistory(
                event.transactionId(),
                event.accountId(),
                event.userId(),
                event.type(),
                event.amount(),
                event.currency(),
                event.targetAccountId(),
                TransactionStatus.COMPLETED,
                event.createdAt(),
                null
        );

        historyRepository.save(history);

        log.info("Transaction COMPLETED for transaction: {}", event.transactionId());
    }

    public void handleApproved(TransactionApprovedEvent event) {
        TransactionHistory history = new TransactionHistory(
                event.transactionId(),
                event.accountId(),
                event.userId(),
                event.type(),
                event.amount(),
                event.currency(),
                event.targetAccountId(),
                TransactionStatus.COMPLETED,
                event.createdAt(),
                null
        );

        historyRepository.save(history);

        log.info("Transaction COMPLETED for: {}", event.transactionId());
    }

    public void handleDeclined(TransactionDeclinedEvent event) {
        TransactionHistory history = new TransactionHistory(
                event.transactionId(),
                event.accountId(),
                event.userId(),
                event.type(),
                event.amount(),
                event.currency(),
                event.targetAccountId(),
                TransactionStatus.FAILED,
                event.createdAt(),
                event.reason()
        );
        historyRepository.save(history);
        log.info("Transaction FAILED for: {}", event.transactionId());
    }
}