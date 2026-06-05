package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.transaction.core.exceptions.AccountNotActiveException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTransactionHandler {

    protected final IAccountClient accountClient;
    protected final IOutboxRepository outboxRepository;

    protected AccountSnapshot getAccountSnapshot(UUID accountId, String token) {
        AccountSnapshot account = accountClient.getAccount(accountId, token);
        if (!account.status().equals("ACTIVE")) {
            throw new AccountNotActiveException(accountId);
        }
        return account;
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