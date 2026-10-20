package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;
import com.bankflow.transaction.application.commands.DepositCommand;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.dtos.TransactionCreatedResponse;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepositStatusTest {

    @Test
    void depositStaysPendingUntilLedgerConfirmsIt() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionAuthorizationTest.StubAccountDirectory accounts = new TransactionAuthorizationTest.StubAccountDirectory();

        accounts.accounts.put(accountId, new AccountSnapshot(accountId, userId,"RSD", "ACTIVE"));

        TransactionAuthorizationTest.InMemoryEventStore eventStore = new TransactionAuthorizationTest.InMemoryEventStore();

        IdempotencyGuard guard = new IdempotencyGuard(new InMemoryIdempotencyRepositoryTest(), Runnable::run);

        DepositCommandHandler handler = new DepositCommandHandler(accounts, entry -> {}, guard, eventStore);

        TransactionCreatedResponse response = handler.handle(new DepositCommand(
                new AccountTransaction(accountId, new BigDecimal("100.00"), null, userId, "key-1")));

        assertThat(eventStore.load(response.id()))
                .hasValueSatisfying(t -> assertThat(t.getStatus()).isEqualTo(TransactionStatus.PENDING));
    }
}