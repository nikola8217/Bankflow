package com.bankflow.transaction.application.handlers;

import com.bankflow.shared.enums.TransactionStatus;
import com.bankflow.transaction.application.commands.FailTransactionCommand;
import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.application.bus.CommandHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FailTransactionHandler implements CommandHandler<FailTransactionCommand, Void> {

    private final EventStore eventStore;

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

        if (aggregate.getStatus() == TransactionStatus.FAILED) {
            log.info("Transaction {} already failed, ignoring duplicate event", command.transactionId());
            return null;
        }

        aggregate.fail(command.reason());
        eventStore.save(aggregate);

        log.info("Transaction failed: {}", command.transactionId());
        return null;
    }
}