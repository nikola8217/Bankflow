package com.bankflow.account.persistence.adapters;

import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.domain.models.Account;
import com.bankflow.outbox.OutboxMessage;
import com.bankflow.outbox.OutboxWriter;
import com.bankflow.shared.events.AccountCloseRequestedEvent;
import com.bankflow.shared.events.AccountClosedEvent;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.bankflow.shared.events.Topics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxAccountEventPublisher implements AccountEventPublisher {

    private final OutboxWriter outboxWriter;

    @Override
    public void accountCreated(Account account) {
        publish(account, Topics.ACCOUNT_CREATED, new AccountCreatedEvent(
                account.getId(), account.getUserId(), account.getCurrency().name()));
    }

    @Override
    public void closeRequested(Account account) {
        publish(account, Topics.ACCOUNT_CLOSE_REQUESTED, new AccountCloseRequestedEvent(
                account.getId(), account.getUserId()));
    }

    @Override
    public void accountClosed(Account account) {
        publish(account, Topics.ACCOUNT_CLOSED, new AccountClosedEvent(
                account.getId(), account.getUserId(), account.getCurrency().name()));
    }

    private void publish(Account account, String topic, Object event) {
        outboxWriter.append(new OutboxMessage(account.getId(), topic, account.getId().toString(), event));
    }
}