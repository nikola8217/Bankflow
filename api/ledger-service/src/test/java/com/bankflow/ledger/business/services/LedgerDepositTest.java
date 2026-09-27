package com.bankflow.ledger.business.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.ledger.infra.models.OutboxModel;
import com.bankflow.ledger.infra.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerDepositTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    OutboxJpaRepository outboxRepository;

    @Test
    void bookedDepositIsConfirmedBackToTransactionService() {
        UUID accountId = UUID.randomUUID();
        ledgerService.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));

        TransactionCreatedEvent deposit = new TransactionCreatedEvent(
                UUID.randomUUID(), accountId, UUID.randomUUID(),
                TransactionType.DEPOSIT, new BigDecimal("100.00"), "RSD", null, LocalDateTime.now());

        ledgerService.process(deposit);

        assertThat(outboxRepository.findAll())
                .filteredOn(entry -> entry.getAggregateId().equals(deposit.transactionId()))
                .extracting(OutboxModel::getEventType)
                .containsExactly("TransactionApprovedEvent");
    }
}