package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class WithdrawCommandHandler extends BaseTransactionHandler implements CommandHandler<WithdrawCommand, TransactionCreatedResponse> {

    private final IEventStore eventStore;

    public WithdrawCommandHandler(IAccountClient accountClient,
                                 IOutboxRepository outboxRepository,
                                 IIdempotencyRepository idempotencyRepository,
                                 IEventStore eventStore) {
        super(accountClient, outboxRepository, idempotencyRepository);
        this.eventStore = eventStore;
    }

    @Override
    public Class<WithdrawCommand> getCommandType() {
        return WithdrawCommand.class;
    }

    @Override
    @Transactional
    public TransactionCreatedResponse handle(WithdrawCommand command) {
        checkIdempotency(command.idempotencyKey());

        AccountSnapshot account = getAccountSnapshot(command.dto().accountId(), command.token());

        UUID transactionId = UUID.randomUUID();

        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(
                transactionId,
                account.id(),
                command.userId(),
                TransactionType.WITHDRAWAL,
                command.dto().amount(),
                account.currency(),
                null
        );

        eventStore.save(aggregate);
        saveToOutbox(transactionId, account.id(), command.userId(),
                TransactionType.WITHDRAWAL, command.dto().amount(), account.currency(), null);

        saveIdempotencyKey(command.idempotencyKey());

        return TransactionCreatedResponse.from(transactionId, "Withdrawal initiated successfully");
    }
}