package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.TransferCommand;
import com.bankflow.transaction.business.dtos.TransferDto;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.ports.ITransactionRunner;
import com.bankflow.transaction.business.responses.TransferResponse;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.core.exceptions.TransactionException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransferCommandHandler extends BaseTransactionHandler
        implements CommandHandler<TransferCommand, TransferResponse> {

    private final IEventStore eventStore;

    public TransferCommandHandler(IAccountClient accountClient,
                                  IOutboxRepository outboxRepository,
                                  IIdempotencyRepository idempotencyRepository,
                                  ITransactionRunner transactionRunner,
                                  IEventStore eventStore) {
        super(accountClient, outboxRepository, idempotencyRepository, transactionRunner);
        this.eventStore = eventStore;
    }

    @Override
    public Class<TransferCommand> getCommandType() {
        return TransferCommand.class;
    }

    @Override
    public TransferResponse handle(TransferCommand command) {
        TransferDto dto = command.dto();
        String fingerprint = fingerprint(TransactionType.TRANSFER,
                dto.fromAccountId(), dto.toAccountId(), dto.amount());

        Optional<UUID> previous = findPreviousTransaction(dto.userId(), dto.idempotencyKey(), fingerprint);
        if (previous.isPresent()) {
            return response(previous.get(), dto);
        }

        AccountSnapshot fromAccount = getOwnedActiveAccount(dto.fromAccountId(), dto.userId());
        AccountSnapshot toAccount = getActiveAccount(dto.toAccountId());

        if (!fromAccount.currency().equals(toAccount.currency())) {
            throw new TransactionException(
                    "Currency mismatch: " + fromAccount.currency() + " vs " + toAccount.currency(),
                    HttpStatus.BAD_REQUEST
            );
        }

        UUID transactionId = UUID.randomUUID();
        try {
            transactionRunner.inTransaction(() -> {
                saveIdempotencyKey(dto.userId(), dto.idempotencyKey(), fingerprint, transactionId);

                TransactionAggregate aggregate = new TransactionAggregate();
                aggregate.initiate(transactionId, fromAccount.id(), dto.userId(),
                        TransactionType.TRANSFER, dto.amount(), fromAccount.currency(), dto.toAccountId());
                eventStore.save(aggregate);

                saveToOutbox(transactionId, fromAccount.id(), dto.userId(),
                        TransactionType.TRANSFER, dto.amount(), fromAccount.currency(), dto.toAccountId());
            });
        } catch (IdempotencyKeyConflictException conflict) {
            return response(transactionOfConcurrentDuplicate(
                    dto.userId(), dto.idempotencyKey(), fingerprint, conflict), dto);
        }

        return response(transactionId, dto);
    }

    private TransferResponse response(UUID transactionId, TransferDto dto) {
        return TransferResponse.from(transactionId, dto.toAccountId(), "Transfer initiated successfully");
    }
}