package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.transaction.core.exceptions.AccountNotActiveException;
import com.bankflow.transaction.core.exceptions.AccountNotFoundException;
import com.bankflow.transaction.core.exceptions.TransactionException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTransactionHandler {

    protected final IAccountClient accountClient;
    protected final IOutboxRepository outboxRepository;
    protected final IIdempotencyRepository idempotencyRepository;

    protected void checkIdempotency(String idempotencyKey) {
        if (idempotencyRepository.exists(idempotencyKey)) {
            throw new TransactionException("Duplicate request", HttpStatus.CONFLICT);
        }
    }

    protected void saveIdempotencyKey(String idempotencyKey) {
        idempotencyRepository.save(idempotencyKey);
    }

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