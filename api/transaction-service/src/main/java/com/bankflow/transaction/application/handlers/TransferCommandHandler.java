package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.TransactionType;
import com.bankflow.transaction.application.commands.TransferCommand;
import com.bankflow.transaction.application.dtos.Transfer;
import com.bankflow.transaction.application.idempotency.IdempotencyGuard;
import com.bankflow.transaction.application.idempotency.IdempotentRequest;
import com.bankflow.transaction.application.ports.AccountClient;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.application.ports.TransactionEventPublisher;
import com.bankflow.transaction.application.dtos.TransferResponse;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.application.bus.CommandHandler;
import com.bankflow.transaction.domain.exceptions.TransactionException;
import com.bankflow.transaction.domain.models.AccountSnapshot;
import com.bankflow.shared.exceptions.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class TransferCommandHandler extends BaseTransactionHandler
        implements CommandHandler<TransferCommand, TransferResponse> {

    private final IdempotencyGuard idempotency;
    private final EventStore eventStore;

    public TransferCommandHandler(AccountClient accountClient,
                                  TransactionEventPublisher eventPublisher,
                                  IdempotencyGuard idempotency,
                                  EventStore eventStore) {
        super(accountClient, eventPublisher);
        this.idempotency = idempotency;
        this.eventStore = eventStore;
    }

    @Override
    public Class<TransferCommand> getCommandType() {
        return TransferCommand.class;
    }

    @Override
    public TransferResponse handle(TransferCommand command) {
        Transfer dto = command.dto();
        IdempotentRequest request = IdempotentRequest.of(dto.userId(), dto.idempotencyKey(),
                TransactionType.TRANSFER, dto.fromAccountId(), dto.toAccountId(), dto.amount());

        Optional<UUID> previous = idempotency.previousResult(request);
        if (previous.isPresent()) {
            return response(previous.get(), dto);
        }

        AccountSnapshot fromAccount = getOwnedActiveAccount(dto.fromAccountId(), dto.userId());
        AccountSnapshot toAccount = getActiveAccount(dto.toAccountId());

        if (!fromAccount.currency().equals(toAccount.currency())) {
            throw new TransactionException(
                    "Currency mismatch: " + fromAccount.currency() + " vs " + toAccount.currency(),
                    ErrorType.BUSINESS_RULE
            );
        }

        UUID transactionId = idempotency.executeOnce(request, id -> {
            TransactionAggregate aggregate = new TransactionAggregate();
            aggregate.initiate(id, fromAccount.id(), dto.userId(),
                    TransactionType.TRANSFER, dto.amount(), fromAccount.currency(), dto.toAccountId());
            eventStore.save(aggregate);

            publishTransactionCreated(id, fromAccount.id(), dto.userId(),
                    TransactionType.TRANSFER, dto.amount(), fromAccount.currency(), dto.toAccountId());
        });

        return response(transactionId, dto);
    }

    private TransferResponse response(UUID transactionId, Transfer dto) {
        return TransferResponse.from(transactionId, dto.toAccountId(), "Transfer initiated successfully");
    }
}