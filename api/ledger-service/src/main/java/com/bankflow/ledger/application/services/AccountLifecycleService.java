package com.bankflow.ledger.application.services;

import com.bankflow.ledger.application.ports.BalanceRepository;
import com.bankflow.ledger.application.ports.LedgerEventPublisher;
import com.bankflow.ledger.domain.models.Balance;
import com.bankflow.shared.events.AccountCloseRequestedEvent;
import com.bankflow.shared.events.AccountClosureDecidedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** The Ledger's side of an account's life: it learns about new accounts and decides whether one may be closed. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountLifecycleService {

    private final BalanceRepository balanceRepository;
    private final LedgerEventPublisher eventPublisher;

    @Transactional
    public void registerAccount(AccountCreatedEvent event) {
        balanceRepository.registerAccount(event.accountId(), event.userId(), event.currency());

        log.info("Account registered: {} (owner {})", event.accountId(), event.userId());
    }

    /**
     * Step two of the account-closing saga. The balance row is locked, so no booking can slip in between the
     * zero-balance check and the close: once closed here, every later deposit, withdrawal or transfer is declined.
     * Safe to redeliver: an already closed account is approved again, a non-empty one rejected again.
     */
    @Transactional
    public void decideClosure(AccountCloseRequestedEvent event) {
        Optional<Balance> locked = balanceRepository.findByAccountIdForUpdate(event.accountId());

        if (locked.isEmpty()) {
            reject(event, "Account is not known to the ledger yet");
            return;
        }

        Balance balance = locked.get();

        if (!balance.canClose()) {
            reject(event, "Balance is not zero");
            return;
        }

        if (!balance.isClosed()) {
            balance.close();
            balanceRepository.save(balance);
        }
        eventPublisher.accountClosureDecided(AccountClosureDecidedEvent.approved(event.accountId()));

        log.info("Account closure approved: {}", event.accountId());
    }

    private void reject(AccountCloseRequestedEvent event, String reason) {
        eventPublisher.accountClosureDecided(AccountClosureDecidedEvent.rejected(event.accountId(), reason));

        log.info("Account closure rejected: {} ({})", event.accountId(), reason);
    }
}