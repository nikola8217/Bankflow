package com.bankflow.transaction.application.idempotency;

import com.bankflow.transaction.application.ports.IdempotencyRepository;
import com.bankflow.transaction.application.ports.TransactionRunner;
import com.bankflow.transaction.domain.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.domain.exceptions.IdempotencyKeyReusedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class IdempotencyGuard {

    private final IdempotencyRepository repository;
    private final TransactionRunner transactionRunner;

    public Optional<UUID> previousResult(IdempotentRequest request) {
        return repository.find(request.userId(), request.key())
                .map(previous -> {
                    if (!previous.isSameRequestAs(request.fingerprint())) {
                        throw new IdempotencyKeyReusedException();
                    }
                    return previous.transactionId();
                });
    }

    public UUID executeOnce(IdempotentRequest request, Consumer<UUID> writes) {
        UUID transactionId = UUID.randomUUID();
        try {
            transactionRunner.inTransaction(() -> {
                repository.save(new IdempotencyRecord(request.userId(), request.key(),
                        request.fingerprint(), transactionId, LocalDateTime.now()));
                writes.accept(transactionId);
            });
            return transactionId;
        } catch (IdempotencyKeyConflictException conflict) {
            return previousResult(request).orElseThrow(() -> conflict);
        }
    }
}