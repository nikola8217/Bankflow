package com.bankflow.transaction.messaging;

import com.bankflow.shared.events.AccountClosedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.Topics;
import com.bankflow.transaction.application.bus.CommandBus;
import com.bankflow.transaction.application.commands.MarkAccountClosedCommand;
import com.bankflow.transaction.application.commands.RecordAccountCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final CommandBus commandBus;

    @KafkaListener(topics = Topics.ACCOUNT_CREATED, groupId = "transaction-service")
    public void handleCreated(AccountCreatedEvent event) {
        log.info("Received AccountCreatedEvent for: {}", event.accountId());

        commandBus.send(new RecordAccountCommand(event.accountId(), event.userId(), event.currency()));
    }

    @KafkaListener(topics = Topics.ACCOUNT_CLOSED, groupId = "transaction-service")
    public void handleClosed(AccountClosedEvent event) {
        log.info("Received AccountClosedEvent for: {}", event.accountId());

        commandBus.send(new MarkAccountClosedCommand(event.accountId(), event.userId(), event.currency()));
    }
}