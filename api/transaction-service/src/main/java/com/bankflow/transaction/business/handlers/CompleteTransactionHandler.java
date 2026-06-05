package com.bankflow.transaction.business.handlers;

import com.bankflow.transaction.business.commands.CompleteTransactionCommand;
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
public class CompleteTransactionHandler implements CommandHandler<CompleteTransactionCommand, Void> {

    private final IEventStore eventStore;

    @Override
    public Class<CompleteTransactionCommand> getCommandType() {
        return CompleteTransactionCommand.class;
    }

    @Override
    @Transactional
    public Void handle(CompleteTransactionCommand command) {
        TransactionAggregate aggregate = eventStore.load(command.transactionId())
                .orElseThrow(() -> new RuntimeException(
                        "Transaction not found: " + command.transactionId()
                ));

        aggregate.complete();
        eventStore.save(aggregate);

        log.info("Transaction completed: {}", command.transactionId());
        return null;
    }
}