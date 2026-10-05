package com.bankflow.ledger.application.projections;

import com.bankflow.ledger.application.ports.TransactionHistoryRepository;
import com.bankflow.ledger.domain.models.TransactionHistory;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionHistoryProjection {

    private final TransactionHistoryRepository historyRepository;

    public void project(TransactionCreatedEvent event, TransactionStatus status, String reason) {
        historyRepository.save(new TransactionHistory(
                event.transactionId(), event.accountId(), event.userId(),
                event.type(), event.amount(), event.currency(),
                event.targetAccountId(), status, event.createdAt(), reason
        ));
    }
}