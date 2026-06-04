package com.bankflow.ledger.business.projections;

import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.events.TransactionCreatedEvent;
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
                event.createdAt()
        );

        historyRepository.save(history);

        log.info("Transaction history saved for transaction: {}", event.transactionId());
    }
}