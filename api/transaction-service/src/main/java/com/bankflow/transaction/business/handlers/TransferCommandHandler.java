package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.TransferCommand;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.responses.TransferResponse;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import com.bankflow.transaction.core.exceptions.TransactionException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class TransferCommandHandler extends BaseTransactionHandler implements CommandHandler<TransferCommand, TransferResponse> {

    private final IEventStore eventStore;

    public TransferCommandHandler(IAccountClient accountClient,
                                 IOutboxRepository outboxRepository,
                                 IIdempotencyRepository idempotencyRepository,
                                 IEventStore eventStore) {
        super(accountClient, outboxRepository, idempotencyRepository);
        this.eventStore = eventStore;
    }

    @Override
    public Class<TransferCommand> getCommandType() {
        return TransferCommand.class;
    }

    @Override
    @Transactional
    public TransferResponse handle(TransferCommand command) {
        checkIdempotency(command.dto().idempotencyKey());

        AccountSnapshot fromAccount = getAccountSnapshot(command.dto().fromAccountId(), command.dto().token());
        AccountSnapshot toAccount = getAccountSnapshot(command.dto().toAccountId(), command.dto().token());

        if (!fromAccount.currency().equals(toAccount.currency())) {
            throw new TransactionException(
                    "Currency mismatch: " + fromAccount.currency() + " vs " + toAccount.currency(),
                    HttpStatus.BAD_REQUEST
            );
        }

        UUID transactionId = UUID.randomUUID();

        TransactionAggregate aggregate = new TransactionAggregate();

        aggregate.initiate(
                transactionId,
                fromAccount.id(),
                command.dto().userId(),
                TransactionType.TRANSFER,
                command.dto().amount(),
                fromAccount.currency(),
                command.dto().toAccountId()
        );

        eventStore.save(aggregate);

        saveToOutbox(transactionId, fromAccount.id(), command.dto().userId(),
                TransactionType.TRANSFER, command.dto().amount(),
                fromAccount.currency(), command.dto().toAccountId());

        saveIdempotencyKey(command.dto().idempotencyKey());

        return TransferResponse.from(transactionId, command.dto().toAccountId(), "Transfer initiated successfully");
    }
}