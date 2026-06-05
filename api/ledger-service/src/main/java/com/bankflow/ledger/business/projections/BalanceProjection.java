package com.bankflow.ledger.business.projections;

import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.IOutboxRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.OutboxEntry;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BalanceProjection {

    private final IBalanceRepository balanceRepository;
    private final IOutboxRepository outboxRepository;

    @Transactional
    public void handle(TransactionCreatedEvent event) {
        if (event.type() == TransactionType.DEPOSIT) {
            handleDeposit(event);
        } else if (event.type() == TransactionType.WITHDRAWAL || event.type() == TransactionType.TRANSFER) {
            handleWithdrawalOrTransfer(event);
        }
    }

    private void handleDeposit(TransactionCreatedEvent event) {
        Balance balance = getOrCreateBalance(event.accountId(), event.currency());
        balance.credit(event.amount());
        balanceRepository.save(balance);
        log.info("Deposit processed for account: {}", event.accountId());
    }

    private void handleWithdrawalOrTransfer(TransactionCreatedEvent event) {
        Balance balance = getOrCreateBalance(event.accountId(), event.currency());

        if (balance.getAmount().compareTo(event.amount()) < 0) {
            log.warn("Insufficient funds for transaction: {}", event.transactionId());
            saveToOutbox(event, false, "Insufficient funds");
        } else {
            balance.debit(event.amount());
            balanceRepository.save(balance);
            saveToOutbox(event, true, null);
        }
    }

    public void handleApproved(TransactionApprovedEvent event) {
        if (event.type() == TransactionType.TRANSFER) {
            Balance toBalance = getOrCreateBalance(event.targetAccountId(), event.currency());
            toBalance.credit(event.amount());
            balanceRepository.save(toBalance);
            log.info("Transfer credited to account: {}", event.targetAccountId());
        }
    }

    private Balance getOrCreateBalance(UUID accountId, String currency) {
        return balanceRepository
                .findByAccountId(accountId)
                .orElse(new Balance(accountId, BigDecimal.ZERO, currency));
    }

    private void saveToOutbox(TransactionCreatedEvent event, boolean approved, String reason) {
        Object payload;
        String eventType;

        if (approved) {
            payload = new TransactionApprovedEvent(
                    event.transactionId(), event.accountId(), event.userId(),
                    event.type(), event.amount(), event.currency(),
                    event.targetAccountId(), event.createdAt()
            );
            eventType = "TransactionApprovedEvent";
        } else {
            payload = new TransactionDeclinedEvent(
                    event.transactionId(), event.accountId(), event.userId(),
                    event.type(), event.amount(), event.currency(),
                    event.targetAccountId(), reason, event.createdAt()
            );
            eventType = "TransactionDeclinedEvent";
        }

        OutboxEntry entry = OutboxEntry.builder()
                .aggregateId(event.transactionId())
                .eventType(eventType)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(entry);
    }
}