package com.bankflow.account.persistence.adapters;

import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.domain.models.Account;
import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.AccountCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxAccountEventPublisher implements AccountEventPublisher {

    private static final String ACCOUNT_CREATED_TOPIC = "account-created";

    private final OutboxWriter outboxWriter;

    @Override
    public void accountCreated(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getId(),
                account.getUserId(),
                account.getCurrency().name()
        );

        outboxWriter.append(new OutboxMessage(
                account.getId(),
                ACCOUNT_CREATED_TOPIC,
                account.getId().toString(),
                event));
    }
}