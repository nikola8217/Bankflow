package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IIdempotencyRepository;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.ports.ITransactionRunner;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import com.bankflow.transaction.core.exceptions.IdempotencyKeyConflictException;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WithdrawCommandHandler extends BaseTransactionHandler
        implements CommandHandler<WithdrawCommand, TransactionCreatedResponse> {

    private final IEventStore eventStore;

    public WithdrawCommandHandler(IAccountClient accountClient,
                                  IOutboxRepository outboxRepository,
                                  IIdempotencyRepository idempotencyRepository,
                                  ITransactionRunner transactionRunner,
                                  IEventStore eventStore) {
        super(accountClient, outboxRepository, idempotencyRepository, transactionRunner);
        this.eventStore = eventStore;
    }

    @Override
    public Class<WithdrawCommand> getCommandType() {
        return WithdrawCommand.class;
    }

    @Override
    public TransactionCreatedResponse handle(WithdrawCommand command) {
        AccountTransactionDto dto = command.dto();
        String fingerprint = fingerprint(TransactionType.WITHDRAWAL, dto.accountId(), null, dto.amount());

        Optional<UUID> previous = findPreviousTransaction(dto.userID(), dto.idempotencyKey(), fingerprint);
        if (previous.isPresent()) {
            return response(previous.get());
        }

        AccountSnapshot account = getOwnedActiveAccount(dto.accountId(), dto.userID());

        UUID transactionId = UUID.randomUUID();
        try {
            transactionRunner.inTransaction(() -> {
                saveIdempotencyKey(dto.userID(), dto.idempotencyKey(), fingerprint, transactionId);

                TransactionAggregate aggregate = new TransactionAggregate();
                aggregate.initiate(transactionId, account.id(), dto.userID(),
                        TransactionType.WITHDRAWAL, dto.amount(), account.currency(), null);
                eventStore.save(aggregate);

                saveToOutbox(transactionId, account.id(), dto.userID(),
                        TransactionType.WITHDRAWAL, dto.amount(), account.currency(), null);
            });
        } catch (IdempotencyKeyConflictException conflict) {
            return response(transactionOfConcurrentDuplicate(
                    dto.userID(), dto.idempotencyKey(), fingerprint, conflict));
        }

        return response(transactionId);
    }

    private TransactionCreatedResponse response(UUID transactionId) {
        return TransactionCreatedResponse.from(transactionId, "Withdrawal initiated successfully");
    }
}