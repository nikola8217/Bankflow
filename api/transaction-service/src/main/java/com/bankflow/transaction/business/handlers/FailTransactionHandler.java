package com.bankflow.transaction.business.handlers;

import com.bankflow.transaction.business.commands.FailTransactionCommand;
import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.core.commands.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailTransactionHandler implements CommandHandler<FailTransactionCommand, Void> {

    private final IEventStore eventStore;

    @Override
    public Class<FailTransactionCommand> getCommandType() {
        return FailTransactionCommand.class;
    }

    @Override
    @Transactional
    public Void handle(FailTransactionCommand command) {
        TransactionAggregate aggregate = eventStore.load(command.transactionId())
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + command.transactionId()
                ));

        aggregate.fail(command.reason());

        eventStore.save(aggregate);

        log.info("Transaction failed: {}", command.transactionId());
        return null;
    }
}