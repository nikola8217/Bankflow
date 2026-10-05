package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.application.commands.WithdrawCommand;
import com.bankflow.transaction.application.dtos.AccountTransaction;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;
import com.bankflow.transaction.application.idempotency.IdempotentRequest;
import com.bankflow.transaction.application.ports.AccountClient;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import com.bankflow.transaction.application.dtos.TransactionCreatedResponse;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.application.bus.CommandHandler;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class WithdrawCommandHandler extends BaseTransactionHandler
        implements CommandHandler<WithdrawCommand, TransactionCreatedResponse> {

    private final IdempotencyGuard idempotency;
    private final EventStore eventStore;

    public WithdrawCommandHandler(AccountClient accountClient,
                                  TransactionEventPublisher eventPublisher,
                                  IdempotencyGuard idempotency,
                                  EventStore eventStore) {
        super(accountClient, eventPublisher);
        this.idempotency = idempotency;
        this.eventStore = eventStore;
    }

    @Override
    public Class<WithdrawCommand> getCommandType() {
        return WithdrawCommand.class;
    }

    @Override
    public TransactionCreatedResponse handle(WithdrawCommand command) {
        AccountTransaction dto = command.dto();
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

            publishTransactionCreated(id, account.id(), dto.userID(),
                    TransactionType.WITHDRAWAL, dto.amount(), account.currency(), null);
        });

        return response(transactionId);
    }

    private TransactionCreatedResponse response(UUID transactionId) {
        return TransactionCreatedResponse.from(transactionId, "Withdrawal initiated successfully");
    }
}