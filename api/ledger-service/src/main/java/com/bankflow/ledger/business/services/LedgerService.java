package com.bankflow.ledger.business.services;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.IOutboxRepository;
import com.bankflow.ledger.business.ports.IProcessedEventRepository;
import com.bankflow.ledger.business.projections.TransactionHistoryProjection;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.OutboxEntry;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LedgerService {

    private final IBalanceRepository balanceRepository;
    private final IOutboxRepository outboxRepository;
    private final IProcessedEventRepository processedEventRepository;
    private final TransactionHistoryProjection historyProjection;

    @Transactional
    public void process(TransactionCreatedEvent event) {
        String key = event.transactionId() + "_CREATED";
        if (processedEventRepository.exists(key)) {
            log.warn("Event already processed: {}", key);
            return;
        }

        switch (event.type()) {
            case DEPOSIT -> handleDeposit(event);
            case WITHDRAWAL -> handleWithdrawal(event);
            case TRANSFER -> handleTransfer(event);
        }

        processedEventRepository.save(key);
    }

    private void handleDeposit(TransactionCreatedEvent event) {
        Balance balance = lock(event.accountId(), event.currency());
        balance.credit(event.amount());
        balanceRepository.save(balance);
        historyProjection.project(event, TransactionStatus.COMPLETED, null);
        log.info("Deposit processed for account: {}", event.accountId());
    }

    private void handleWithdrawal(TransactionCreatedEvent event) {
        Balance balance = lock(event.accountId(), event.currency());

        if (!balance.canDebit(event.amount())) {
            decline(event, "Insufficient funds");
            return;
        }

        balance.debit(event.amount());
        balanceRepository.save(balance);
        approve(event);
    }

    private void handleTransfer(TransactionCreatedEvent event) {
        UUID fromId = event.accountId();
        UUID toId = event.targetAccountId();

        if (fromId.equals(toId)) {
            decline(event, "Cannot transfer to the same account");
            return;
        }

        boolean fromFirst = fromId.compareTo(toId) < 0;
        Balance first = lock(fromFirst ? fromId : toId, event.currency());
        Balance second = lock(fromFirst ? toId : fromId, event.currency());
        Balance from = fromFirst ? first : second;
        Balance to = fromFirst ? second : first;

        if (!from.canDebit(event.amount())) {
            decline(event, "Insufficient funds");
            return;
        }

        from.debit(event.amount());
        to.credit(event.amount());
        balanceRepository.save(from);
        balanceRepository.save(to);
        approve(event);
    }

    private Balance lock(UUID accountId, String currency) {
        balanceRepository.ensureExists(accountId, currency);
        return balanceRepository.findByAccountIdForUpdate(accountId)
                .orElseThrow(() -> new IllegalStateException("Balance row missing for account " + accountId));
    }

    private void approve(TransactionCreatedEvent event) {
        saveToOutbox("TransactionApprovedEvent", event.transactionId(), new TransactionApprovedEvent(
                event.transactionId(), event.accountId(), event.userId(),
                event.type(), event.amount(), event.currency(),
                event.targetAccountId(), event.createdAt()
        ));
        historyProjection.project(event, TransactionStatus.COMPLETED, null);
        log.info("Transaction approved: {}", event.transactionId());
    }

    private void decline(TransactionCreatedEvent event, String reason) {
        saveToOutbox("TransactionDeclinedEvent", event.transactionId(), new TransactionDeclinedEvent(
                event.transactionId(), event.accountId(), event.userId(),
                event.type(), event.amount(), event.currency(),
                event.targetAccountId(), reason, event.createdAt()
        ));
        historyProjection.project(event, TransactionStatus.FAILED, reason);
        log.warn("Transaction declined: {} ({})", event.transactionId(), reason);
    }

    private void saveToOutbox(String eventType, UUID aggregateId, Object payload) {
        outboxRepository.save(OutboxEntry.builder()
                .aggregateId(aggregateId)
                .eventType(eventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());
    }
}