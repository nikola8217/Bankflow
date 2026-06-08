package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.DepositCommand;
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
public class DepositCommandHandler extends BaseTransactionHandler implements CommandHandler<DepositCommand, TransactionCreatedResponse> {

    private final IEventStore eventStore;

    public DepositCommandHandler(IAccountClient accountClient,
                                 IOutboxRepository outboxRepository,
                                 IIdempotencyRepository idempotencyRepository,
                                 IEventStore eventStore) {
        super(accountClient, outboxRepository, idempotencyRepository);
        this.eventStore = eventStore;
    }

    @Override
    public Class<DepositCommand> getCommandType() {
        return DepositCommand.class;
    }

    @Override
    @Transactional
    public TransactionCreatedResponse handle(DepositCommand command) {
        checkIdempotency(command.dto().idempotencyKey());

        AccountSnapshot account = getAccountSnapshot(command.dto().accountId(), command.dto().token());

        UUID transactionId = UUID.randomUUID();

        TransactionAggregate aggregate = new TransactionAggregate();
        aggregate.initiate(
                transactionId,
                account.id(),
                command.dto().userID(),
                TransactionType.DEPOSIT,
                command.dto().amount(),
                account.currency(),
                null
        );
        aggregate.complete();

        eventStore.save(aggregate);

        saveToOutbox(transactionId, account.id(), command.dto().userID(),
                TransactionType.DEPOSIT, command.dto().amount(), account.currency(), null);

        saveIdempotencyKey(command.dto().idempotencyKey());

        return TransactionCreatedResponse.from(transactionId, "Deposit initiated successfully");
    }
}