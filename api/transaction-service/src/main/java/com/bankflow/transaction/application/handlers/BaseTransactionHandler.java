package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.transaction.application.ports.AccountDirectory;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import com.bankflow.transaction.domain.exceptions.AccountNotActiveException;
import com.bankflow.transaction.domain.exceptions.AccountNotFoundException;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public abstract class BaseTransactionHandler {

    protected final AccountDirectory accounts;
    protected final TransactionEventPublisher eventPublisher;

    protected AccountSnapshot getActiveAccount(UUID accountId) {
        AccountSnapshot account = findAccount(accountId);

        ensureActive(account);

        return account;
    }

    protected AccountSnapshot getOwnedActiveAccount(UUID accountId, UUID userId) {
        AccountSnapshot account = findAccount(accountId);

        if (!account.userId().equals(userId)) {
            throw new AccountNotFoundException(accountId);
        }

        ensureActive(account);

        return account;
    }

    private AccountSnapshot findAccount(UUID accountId) {
        return accounts.findAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
    }

    private void ensureActive(AccountSnapshot account) {
        if (!"ACTIVE".equals(account.status())) {
            throw new AccountNotActiveException(account.id());
        }
    }

    protected void publishTransactionCreated(UUID transactionId, UUID accountId, UUID userId,
                                             TransactionType type, BigDecimal amount,
                                             String currency, UUID targetAccountId) {
        eventPublisher.transactionCreated(new TransactionCreatedEvent(
                transactionId, accountId, userId,
                type, amount, currency, targetAccountId,
                LocalDateTime.now()
        ));
    }
}