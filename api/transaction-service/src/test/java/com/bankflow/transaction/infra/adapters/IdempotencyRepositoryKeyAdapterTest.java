package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.AbstractIntegrationTest;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.ITransactionRunner;
import com.bankflow.transaction.core.entities.IdempotencyRecord;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdempotencyRepositoryAdapterTest extends AbstractIntegrationTest {

    @Autowired
    IIdempotencyRepository repository;

    @Autowired
    ITransactionRunner transactionRunner;

    @Test
    void storedKeyCanBeFoundByUserAndKey() {
        UUID user = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        repository.save(record(user, "key-1", transactionId));

        assertThat(repository.find(user, "key-1"))
                .hasValueSatisfying(r -> assertThat(r.transactionId()).isEqualTo(transactionId));

        assertThat(repository.find(UUID.randomUUID(), "key-1")).isEmpty();
    }

    @Test
    void sameUserCannotStoreTheSameKeyTwice() {
        UUID user = UUID.randomUUID();
        repository.save(record(user, "key-1", UUID.randomUUID()));

        assertThatThrownBy(() -> repository.save(record(user, "key-1", UUID.randomUUID())))
                .isInstanceOf(IdempotencyKeyConflictException.class);   // ne DataIntegrityViolationException, ne 500
    }

    @Test
    void differentUsersCanStoreTheSameKey() {
        repository.save(record(UUID.randomUUID(), "same-key", UUID.randomUUID()));
        repository.save(record(UUID.randomUUID(), "same-key", UUID.randomUUID()));   // ne sme da pukne
    }

    @Test
    void onlyOneOfManyConcurrentRequestsWins() throws Exception {
        UUID user = UUID.randomUUID();
        int requests = 8;

        ExecutorService pool = Executors.newFixedThreadPool(requests);
        CountDownLatch startGate = new CountDownLatch(1);
        AtomicInteger conflicts = new AtomicInteger();
        Set<UUID> winners = ConcurrentHashMap.newKeySet();
        List<Future<?>> results = new ArrayList<>();

        for (int i = 0; i < requests; i++) {
            results.add(pool.submit(() -> {
                startGate.await();
                UUID myTransaction = UUID.randomUUID();
                try {
                    transactionRunner.inTransaction(() -> repository.save(record(user, "race-key", myTransaction)));
                    winners.add(myTransaction);
                } catch (IdempotencyKeyConflictException e) {
                    conflicts.incrementAndGet();
                }
                return null;
            }));
        }

        startGate.countDown();
        for (Future<?> result : results) {
            result.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(winners).hasSize(1);
        assertThat(conflicts.get()).isEqualTo(requests - 1);
        assertThat(repository.find(user, "race-key"))
                .hasValueSatisfying(r -> assertThat(r.transactionId()).isEqualTo(winners.iterator().next()));
    }

    private IdempotencyRecord record(UUID userId, String key, UUID transactionId) {
        return new IdempotencyRecord(userId, key, "DEPOSIT|acc|null|100", transactionId, LocalDateTime.now());
    }
}