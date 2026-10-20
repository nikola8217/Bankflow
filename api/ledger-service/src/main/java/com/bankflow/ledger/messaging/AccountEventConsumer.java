package com.bankflow.ledger.messaging;

import com.bankflow.ledger.application.services.AccountLifecycleService;
import com.bankflow.shared.events.AccountCloseRequestedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventConsumer {

    private final AccountLifecycleService accountLifecycle;

    @KafkaListener(topics = Topics.ACCOUNT_CREATED, groupId = "ledger-service")
    public void consumeCreated(AccountCreatedEvent event) {
        log.info("Received AccountCreatedEvent for account: {}", event.accountId());

        accountLifecycle.registerAccount(event);
    }

    @KafkaListener(topics = Topics.ACCOUNT_CLOSE_REQUESTED, groupId = "ledger-service")
    public void consumeCloseRequested(AccountCloseRequestedEvent event) {
        log.info("Received AccountCloseRequestedEvent for account: {}", event.accountId());

        accountLifecycle.decideClosure(event);
    }
}