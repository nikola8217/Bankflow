package com.bankflow.ledger.business.projections;

import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionHistoryProjection {

    private final ITransactionHistoryRepository historyRepository;

    public void project(TransactionCreatedEvent event, TransactionStatus status, String reason) {
        historyRepository.save(new TransactionHistory(
                event.transactionId(), event.accountId(), event.userId(),
                event.type(), event.amount(), event.currency(),
                event.targetAccountId(), status, event.createdAt(), reason
        ));
    }
}