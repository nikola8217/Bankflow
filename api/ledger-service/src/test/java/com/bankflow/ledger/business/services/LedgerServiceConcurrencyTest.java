package com.bankflow.ledger.business.services;

import com.bankflow.ledger.AbstractIntegrationTest;
import com.bankflow.ledger.business.ports.IBalanceRepository;
import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.Balance;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.TransactionCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class LedgerServiceConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    LedgerService ledgerService;

    @Autowired
    IBalanceRepository balanceRepository;

    @Autowired
    ITransactionHistoryRepository historyRepository;

    @Test
    void concurrentWithdrawalsNeverOverdrawTheAccount() throws Exception {
        UUID accountId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        ledgerService.registerAccount(new AccountCreatedEvent(accountId, userId, "RSD"));
        ledgerService.process(event(accountId, userId, TransactionType.DEPOSIT, "1000.00"));

        int requests = 50;

        ExecutorService pool = Executors.newFixedThreadPool(requests);

        CountDownLatch startGate = new CountDownLatch(1);

        List<Future<?>> results = new ArrayList<>();

        for (int i = 0; i < requests; i++) {
            results.add(pool.submit(() -> {
                startGate.await();
                ledgerService.process(event(accountId, userId, TransactionType.WITHDRAWAL, "100.00"));
                return null;
            }));
        }

        startGate.countDown();

        for (Future<?> result : results) {
            result.get(60, TimeUnit.SECONDS);
        }
        pool.shutdown();

        Balance balance = balanceRepository.findByAccountId(accountId).orElseThrow();
        assertThat(balance.getAmount()).isEqualByComparingTo("0");

        List<TransactionHistory> history = historyRepository.findAllByAccountId(accountId);

        long approved = history.stream()
                .filter(h -> h.getType() == TransactionType.WITHDRAWAL)
                .filter(h -> h.getStatus() == TransactionStatus.COMPLETED)
                .count();

        long declined = history.stream()
                .filter(h -> h.getType() == TransactionType.WITHDRAWAL)
                .filter(h -> h.getStatus() == TransactionStatus.FAILED)
                .count();

        assertThat(approved).isEqualTo(10);
        assertThat(declined).isEqualTo(40);
    }

    private TransactionCreatedEvent event(UUID accountId, UUID userId, TransactionType type, String amount) {
        return new TransactionCreatedEvent(
                UUID.randomUUID(),
                accountId,
                userId,
                type,
                new BigDecimal(amount),
                "RSD",
                null,
                LocalDateTime.now()
        );
    }
}