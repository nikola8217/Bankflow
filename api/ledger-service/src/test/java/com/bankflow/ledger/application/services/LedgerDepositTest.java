package com.bankflow.ledger.application.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionApprovedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerDepositTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void bookedDepositIsConfirmedBackToTransactionService() {
        UUID accountId = UUID.randomUUID();
        ledgerService.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));

        TransactionCreatedEvent deposit = new TransactionCreatedEvent(
                UUID.randomUUID(), accountId, UUID.randomUUID(),
                TransactionType.DEPOSIT, new BigDecimal("100.00"), "RSD", null, LocalDateTime.now());

        ledgerService.process(deposit);

        assertThat(jdbcTemplate.queryForList(
                "SELECT event_type FROM outbox WHERE aggregate_id = ?", String.class, deposit.transactionId()))
                .containsExactly(TransactionApprovedEvent.class.getName());
    }
}