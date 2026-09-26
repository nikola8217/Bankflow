package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.ports.ITransactionRunner;
import com.bankflow.transaction.core.entities.IdempotencyRecord;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.transaction.core.exceptions.AccountNotActiveException;
import com.bankflow.transaction.core.exceptions.AccountNotFoundException;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyReusedException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTransactionHandler {

    protected final IAccountClient accountClient;
    protected final IOutboxRepository outboxRepository;
    protected final IIdempotencyRepository idempotencyRepository;
    protected final ITransactionRunner transactionRunner;

    static String fingerprint(TransactionType type, UUID accountId, UUID targetAccountId, BigDecimal amount) {
        return type + "|" + accountId + "|" + targetAccountId + "|" + amount.stripTrailingZeros().toPlainString();
    }

    protected Optional<UUID> findPreviousTransaction(UUID userId, String idempotencyKey, String fingerprint) {
        return idempotencyRepository.find(userId, idempotencyKey)
                .map(previous -> {
                    if (!previous.isSameRequestAs(fingerprint)) {
                        throw new IdempotencyKeyReusedException();
                    }
                    return previous.transactionId();
                });
    }

    protected UUID transactionOfConcurrentDuplicate(UUID userId, String idempotencyKey, String fingerprint,
                                                    IdempotencyKeyConflictException conflict) {
        return findPreviousTransaction(userId, idempotencyKey, fingerprint)
                .orElseThrow(() -> conflict);
    }

    protected void saveIdempotencyKey(UUID userId, String idempotencyKey, String fingerprint, UUID transactionId) {
        idempotencyRepository.save(new IdempotencyRecord(
                userId, idempotencyKey, fingerprint, transactionId, LocalDateTime.now()));
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