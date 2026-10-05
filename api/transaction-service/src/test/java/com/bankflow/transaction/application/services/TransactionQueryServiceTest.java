package com.bankflow.transaction.application.services;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.dtos.TransactionStatusResponse;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.domain.exceptions.TransactionNotFoundException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionQueryServiceTest {

    private final Map<UUID, TransactionAggregate> store = new HashMap<>();

    private final EventStore eventStore = new EventStore() {
        public void save(TransactionAggregate aggregate) { store.put(aggregate.getTransactionId(), aggregate); }
        public Optional<TransactionAggregate> load(UUID id) { return Optional.ofNullable(store.get(id)); }
    };

    private final TransactionQueryService queryService = new TransactionQueryService(eventStore);

    private final UUID ana = UUID.randomUUID();
    private final UUID jelena = UUID.randomUUID();

    @Test
    void ownerSeesPendingWithdrawal() {
        UUID id = withdrawalOf(ana);

        TransactionStatusResponse response = queryService.getTransaction(id, ana);

        assertThat(response.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(response.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(response.failureReason()).isNull();
    }

    @Test
    void ownerSeesWhyWithdrawalFailed() {
        UUID id = withdrawalOf(ana);
        store.get(id).fail("Insufficient funds");

        TransactionStatusResponse response = queryService.getTransaction(id, ana);

        assertThat(response.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("Insufficient funds");
    }

    @Test
    void otherUserGets404ForSomeoneElsesTransaction() {
        UUID id = withdrawalOf(ana);

        assertThatThrownBy(() -> queryService.getTransaction(id, jelena))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    @Test
    void unknownTransactionIs404() {
        assertThatThrownBy(() -> queryService.getTransaction(UUID.randomUUID(), ana))
                .isInstanceOf(TransactionNotFoundException.class);
    }

    private UUID withdrawalOf(UUID userId) {
        UUID id = UUID.randomUUID();
        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(id, UUID.randomUUID(), userId,
                TransactionType.WITHDRAWAL, new BigDecimal("500.00"), "RSD", null);
        eventStore.save(aggregate);
        return id;
    }
}