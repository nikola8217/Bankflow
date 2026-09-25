package com.bankflow.ledger.business.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerServiceIdempotencyTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    IBalanceRepository balanceRepository;

    @Autowired
    ITransactionHistoryRepository historyRepository;

    @Test
    void sameDepositDeliveredTwiceIsBookedOnce() {
        UUID accountId = newAccount();

        TransactionCreatedEvent deposit = event(accountId, TransactionType.DEPOSIT, "1000.00");

        ledgerService.process(deposit);
        ledgerService.process(deposit);

        assertThat(balanceOf(accountId)).isEqualByComparingTo("1000.00");

        assertThat(historyRepository.findAllByAccountId(accountId)).hasSize(1);
    }

    @Test
    void sameWithdrawalDeliveredTwiceIsDebitedOnce() {
        UUID accountId = newAccount();
        ledgerService.process(event(accountId, TransactionType.DEPOSIT, "1000.00"));

        TransactionCreatedEvent withdrawal = event(accountId, TransactionType.WITHDRAWAL, "300.00");

        ledgerService.process(withdrawal);
        ledgerService.process(withdrawal);

        assertThat(balanceOf(accountId)).isEqualByComparingTo("700.00");

        assertThat(historyRepository.findAllByAccountId(accountId)).hasSize(2);
    }

    private UUID newAccount() {
        UUID accountId = UUID.randomUUID();
        ledgerService.registerAccount(new AccountCreatedEvent(accountId, UUID.randomUUID(), "RSD"));
        return accountId;
    }

    private BigDecimal balanceOf(UUID accountId) {
        return balanceRepository.findByAccountId(accountId).orElseThrow().getAmount();
    }

    private TransactionCreatedEvent event(UUID accountId, TransactionType type, String amount) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                accountId,
                UUID.randomUUID(),
                type,
                new BigDecimal(amount),
                "RSD",
                null,
                LocalDateTime.now()
        );
    }
}