package com.bankflow.transaction.business.handlers;

import com.bankflow.transaction.business.commands.DepositCommand;
import com.bankflow.transaction.business.commands.TransferCommand;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.transaction.core.exceptions.AccountNotFoundException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionAuthorizationTest  {

    static class StubAccountClient implements IAccountClient {
        final Map<UUID, AccountSnapshot> accounts = new HashMap<>();

        @Override
        public AccountSnapshot getAccount(UUID accountId) {
            AccountSnapshot account = accounts.get(accountId);
            if (account == null) throw new AccountNotFoundException(accountId);
            return account;
        }
    }

    static class InMemoryEventStore implements IEventStore {
        final Map<UUID, TransactionAggregate> store = new HashMap<>();

        @Override
        public void save(TransactionAggregate aggregate) {
            aggregate.pullDomainEvents();
            store.put(aggregate.getTransactionId(), aggregate);
        }

        @Override
        public Optional<TransactionAggregate> load(UUID transactionId) {
            return Optional.ofNullable(store.get(transactionId));
        }
    }

    static class InMemoryIdempotencyRepository implements IIdempotencyRepository {
        final Set<String> keys = new HashSet<>();

        @Override
        public boolean exists(String key) {
            return keys.contains(key);
        }

        @Override
        public void save(String key) {
            keys.add(key);
        }
    }

    private final List<OutboxEntry> outbox = new ArrayList<>();

    private final IOutboxRepository outboxRepository = outbox::add;

    private final UUID ana = UUID.randomUUID();
    private final UUID jelena = UUID.randomUUID();
    private final UUID anasAccount = UUID.randomUUID();
    private final UUID jelenasAccount = UUID.randomUUID();

    private StubAccountClient accountClient;
    private DepositCommandHandler depositHandler;
    private WithdrawCommandHandler withdrawHandler;
    private TransferCommandHandler transferHandler;

    @BeforeEach
    void setUp() {
        accountClient = new StubAccountClient();
        accountClient.accounts.put(anasAccount,
                new AccountSnapshot(anasAccount, ana, "CHECKING", "RSD", "ACTIVE"));
        accountClient.accounts.put(jelenasAccount,
                new AccountSnapshot(jelenasAccount, jelena, "CHECKING", "RSD", "ACTIVE"));

        InMemoryEventStore eventStore = new InMemoryEventStore();
        InMemoryIdempotencyRepository idempotency = new InMemoryIdempotencyRepository();

        depositHandler = new DepositCommandHandler(accountClient, outboxRepository, idempotency, eventStore);
        withdrawHandler = new WithdrawCommandHandler(accountClient, outboxRepository, idempotency, eventStore);
        transferHandler = new TransferCommandHandler(accountClient, outboxRepository, idempotency, eventStore);
    }

    @Test
    void ownerCanWithdrawFromOwnAccount() {
        withdrawHandler.handle(new WithdrawCommand(withdraw(anasAccount, ana)));

        assertThat(outbox).hasSize(1);   // poruka ide Ledger-u
    }

    @Test
    void otherUserCannotWithdrawFromSomeoneElsesAccount() {
        // Jelena pokušava da podigne novac sa Aninog računa
        assertThatThrownBy(() -> withdrawHandler.handle(new WithdrawCommand(withdraw(anasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        // Najvažnije: NIŠTA nije otišlo u outbox, pa Ledger nikad
        // neće ni saznati za ovaj pokušaj i novac se ne dira.
        assertThat(outbox).isEmpty();
    }

    @Test
    void otherUserCannotDepositToSomeoneElsesAccount() {
        // Dogovor: uplata samo na svoj račun; tuđem se plaća transferom.
        assertThatThrownBy(() -> depositHandler.handle(new DepositCommand(withdraw(anasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(outbox).isEmpty();
    }

    @Test
    void transferToSomeoneElsesAccountIsAllowed() {
        transferHandler.handle(new TransferCommand(transfer(anasAccount, jelenasAccount, ana)));

        assertThat(outbox).hasSize(1);
    }

    @Test
    void cannotTransferFromSomeoneElsesAccount() {
        assertThatThrownBy(() -> transferHandler.handle(new TransferCommand(transfer(anasAccount, jelenasAccount, jelena))))
                .isInstanceOf(AccountNotFoundException.class);

        assertThat(outbox).isEmpty();
    }

    private AccountTransactionDto withdraw(UUID accountId, UUID userId) {
        return new AccountTransactionDto(accountId, new BigDecimal("100.00"), null, userId,
                UUID.randomUUID().toString());
    }

    private TransferDto transfer(UUID from, UUID to, UUID userId) {
        return new TransferDto(from, to, new BigDecimal("100.00"), null, userId,
                UUID.randomUUID().toString());
    }
}