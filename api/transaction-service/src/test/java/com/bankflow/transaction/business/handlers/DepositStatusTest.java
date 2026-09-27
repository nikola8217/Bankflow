package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.transaction.business.IdempotencyGuard;
import com.bankflow.transaction.business.commands.DepositCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DepositStatusTest {

    @Test
    void depositStaysPendingUntilLedgerConfirmsIt() {
        UUID userId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();

        TransactionAuthorizationTest.StubAccountClient accounts = new TransactionAuthorizationTest.StubAccountClient();

        accounts.accounts.put(accountId, new AccountSnapshot(accountId, userId, "CHECKING", "RSD", "ACTIVE"));

        TransactionAuthorizationTest.InMemoryEventStore eventStore = new TransactionAuthorizationTest.InMemoryEventStore();

        IdempotencyGuard guard = new IdempotencyGuard(new InMemoryIdempotencyRepositoryTest(), Runnable::run);

        DepositCommandHandler handler = new DepositCommandHandler(accounts, entry -> {}, guard, eventStore);

        TransactionCreatedResponse response = handler.handle(new DepositCommand(
                new AccountTransactionDto(accountId, new BigDecimal("100.00"), null, userId, "key-1")));

        assertThat(eventStore.load(response.id()))
                .hasValueSatisfying(t -> assertThat(t.getStatus()).isEqualTo(TransactionStatus.PENDING));
    }
}