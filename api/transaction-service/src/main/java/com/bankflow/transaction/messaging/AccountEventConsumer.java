package com.bankflow.transaction.messaging;

import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.Topics;
import com.bankflow.transaction.application.bus.CommandBus;
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
}