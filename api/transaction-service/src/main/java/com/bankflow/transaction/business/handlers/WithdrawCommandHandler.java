package com.bankflow.transaction.business.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.business.commands.WithdrawCommand;
import com.bankflow.transaction.business.dtos.AccountTransactionDto;
import com.bankflow.transaction.business.IdempotencyGuard;
import com.bankflow.transaction.business.IdempotentRequest;
import com.bankflow.transaction.business.ports.IAccountClient;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.business.responses.TransactionCreatedResponse;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import com.bankflow.transaction.core.valueObjects.AccountSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WithdrawCommandHandler extends BaseTransactionHandler
        implements CommandHandler<WithdrawCommand, TransactionCreatedResponse> {

    private final IdempotencyGuard idempotency;
    private final IEventStore eventStore;

    public WithdrawCommandHandler(IAccountClient accountClient,
                                  IOutboxRepository outboxRepository,
                                  IdempotencyGuard idempotency,
                                  IEventStore eventStore) {
        super(accountClient, outboxRepository);
        this.idempotency = idempotency;
        this.eventStore = eventStore;
    }

    @Override
    public Class<WithdrawCommand> getCommandType() {
        return WithdrawCommand.class;
    }

    @Override
    public TransactionCreatedResponse handle(WithdrawCommand command) {
        AccountTransactionDto dto = command.dto();
        IdempotentRequest request = IdempotentRequest.of(dto.userID(), dto.idempotencyKey(),
                TransactionType.WITHDRAWAL, dto.accountId(), null, dto.amount());

        Optional<UUID> previous = idempotency.previousResult(request);
        if (previous.isPresent()) {
            return response(previous.get());
        }

        AccountSnapshot account = getOwnedActiveAccount(dto.accountId(), dto.userID());

        UUID transactionId = idempotency.executeOnce(request, id -> {
            TransactionAggregate aggregate = new TransactionAggregate();
            aggregate.initiate(id, account.id(), dto.userID(),
                    TransactionType.WITHDRAWAL, dto.amount(), account.currency(), null);
            eventStore.save(aggregate);

            saveToOutbox(id, account.id(), dto.userID(),
                    TransactionType.WITHDRAWAL, dto.amount(), account.currency(), null);
        });

        return response(transactionId);
    }

    private TransactionCreatedResponse response(UUID transactionId) {
        return TransactionCreatedResponse.from(transactionId, "Withdrawal initiated successfully");
    }
}