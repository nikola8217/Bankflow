package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.application.ports.AccountClient;
import com.bankflow.transaction.application.ports.OutboxRepository;
import com.bankflow.transaction.application.outbox.OutboxEntry;
import com.bankflow.transaction.domain.exceptions.AccountNotActiveException;
import com.bankflow.transaction.domain.exceptions.AccountNotFoundException;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTransactionHandler {

    protected final AccountClient accountClient;
    protected final OutboxRepository outboxRepository;

    protected AccountSnapshot getActiveAccount(UUID accountId) {
        AccountSnapshot account = accountClient.getAccount(accountId);
        ensureActive(account);
        return account;
    }

    protected AccountSnapshot getOwnedActiveAccount(UUID accountId, UUID userId) {
        AccountSnapshot account = accountClient.getAccount(accountId);
        if (!account.userId().equals(userId)) {
            throw new AccountNotFoundException(accountId);
        }
        ensureActive(account);
        return account;
    }

    private void ensureActive(AccountSnapshot account) {
        if (!"ACTIVE".equals(account.status())) {
            throw new AccountNotActiveException(account.id());
        }
    }

    protected void saveToOutbox(UUID transactionId, UUID accountId, UUID userId,
                                TransactionType type, BigDecimal amount,
                                String currency, UUID targetAccountId) {
        OutboxEntry entry = OutboxEntry.builder()
                .aggregateId(transactionId)
                .eventType("TransactionCreatedEvent")
                .payload(new TransactionCreatedEvent(
                        transactionId, accountId, userId,
                        type, amount, currency, targetAccountId,
                        LocalDateTime.now()
                ))
                .status(OutboxStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepository.save(entry);
    }
}