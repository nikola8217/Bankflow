package com.bankflow.ledger.application.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCloseRequestedEvent;
import com.bankflow.shared.events.AccountClosureDecidedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import com.bankflow.shared.events.TransactionDeclinedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The Ledger's half of the account-closing saga: it decides, and once it closes an account no money moves on it. */
class LedgerAccountClosureTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    AccountLifecycleService accountLifecycle;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void emptyAccountIsClosed() {
        UUID accountId = openAccount();

        accountLifecycle.decideClosure(new AccountCloseRequestedEvent(accountId, UUID.randomUUID()));

        assertThat(isClosed(accountId)).isTrue();
        assertThat(decisions(accountId)).containsExactly("{\"approved\": true}");
    }

    @Test
    void accountWithMoneyIsNotClosed() {
        UUID accountId = openAccount();
        ledgerService.process(transaction(TransactionType.DEPOSIT, accountId, null, "50.00"));

        accountLifecycle.decideClosure(new AccountCloseRequestedEvent(accountId, UUID.randomUUID()));

        assertThat(isClosed(accountId)).isFalse();
        assertThat(decisions(accountId)).containsExactly("{\"approved\": false}");
    }

    @Test
    void redeliveredRequestIsApprovedAgainWithoutError() {
        UUID accountId = openAccount();
        AccountCloseRequestedEvent request = new AccountCloseRequestedEvent(accountId, UUID.randomUUID());

        accountLifecycle.decideClosure(request);
        accountLifecycle.decideClosure(request);

        assertThat(isClosed(accountId)).isTrue();
        assertThat(decisions(accountId)).containsExactly("{\"approved\": true}", "{\"approved\": true}");
    }

    @Test
    void closedAccountRefusesDeposits() {
        UUID accountId = openAccount();
        accountLifecycle.decideClosure(new AccountCloseRequestedEvent(accountId, UUID.randomUUID()));

        TransactionCreatedEvent deposit = transaction(TransactionType.DEPOSIT, accountId, null, "10.00");
        ledgerService.process(deposit);

        assertThat(declineReason(deposit)).isEqualTo("Account is closed");
        assertThat(balance(accountId)).isEqualByComparingTo("0.00");
    }

    @Test
    void transferToClosedAccountIsDeclinedAndMoneyStays() {
        UUID from = openAccount();
        UUID closed = openAccount();
        ledgerService.process(transaction(TransactionType.DEPOSIT, from, null, "100.00"));
        accountLifecycle.decideClosure(new AccountCloseRequestedEvent(closed, UUID.randomUUID()));

        TransactionCreatedEvent transfer = transaction(TransactionType.TRANSFER, from, closed, "40.00");
        ledgerService.process(transfer);

        assertThat(declineReason(transfer)).isEqualTo("Account is closed");
        assertThat(balance(from)).isEqualByComparingTo("100.00");
        assertThat(balance(closed)).isEqualByComparingTo("0.00");
    }

    private UUID openAccount() {
        UUID accountId = UUID.randomUUID();
        accountLifecycle.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));
        return accountId;
    }

    private TransactionCreatedEvent transaction(TransactionType type, UUID accountId, UUID target, String amount) {
        return new TransactionCreatedEvent(UUID.randomUUID(), accountId, UUID.randomUUID(),
                type, new BigDecimal(amount), "RSD", target, LocalDateTime.now());
    }

    private boolean isClosed(UUID accountId) {
        return Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT closed FROM balances WHERE account_id = ?", Boolean.class, accountId));
    }

    private BigDecimal balance(UUID accountId) {
        return jdbc.queryForObject("SELECT amount FROM balances WHERE account_id = ?", BigDecimal.class, accountId);
    }

    /** The "approved" flag of every closure decision written to the outbox for the account, oldest first. */
    private List<String> decisions(UUID accountId) {
        return jdbc.queryForList("""
                SELECT jsonb_build_object('approved', payload -> 'approved')::text
                FROM outbox
                WHERE aggregate_id = ? AND event_type = ?
                ORDER BY created_at
                """, String.class, accountId, AccountClosureDecidedEvent.class.getName());
    }

    private String declineReason(TransactionCreatedEvent event) {
        return jdbc.queryForObject("""
                SELECT payload ->> 'reason'
                FROM outbox
                WHERE aggregate_id = ? AND event_type = ?
                """, String.class, event.transactionId(), TransactionDeclinedEvent.class.getName());
    }
}