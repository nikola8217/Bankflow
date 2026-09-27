package com.bankflow.transaction.business;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyGuardTest extends AbstractIntegrationTest {

    @Autowired
    IdempotencyGuard guard;

    @Test
    void parallelDuplicatesAllGetTheSameTransactionAndWritesRunOnce() throws Exception {
        IdempotentRequest request = IdempotentRequest.of(UUID.randomUUID(), "race-key",
                TransactionType.DEPOSIT, UUID.randomUUID(), null, new BigDecimal("100"));

        int requests = 8;
        AtomicInteger writes = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch startGate = new CountDownLatch(1);
        List<Future<UUID>> results = new ArrayList<>();

        for (int i = 0; i < requests; i++) {
            results.add(pool.submit(() -> {
                startGate.await();
                return guard.executeOnce(request, id -> writes.incrementAndGet());
            }));
        }
        startGate.countDown();

        Set<UUID> transactionIds = new HashSet<>();
        for (Future<UUID> result : results) {
            transactionIds.add(result.get(30, TimeUnit.SECONDS));
        }
        pool.shutdown();

        assertThat(transactionIds).hasSize(1);
        assertThat(writes.get()).isEqualTo(1);
    }
}