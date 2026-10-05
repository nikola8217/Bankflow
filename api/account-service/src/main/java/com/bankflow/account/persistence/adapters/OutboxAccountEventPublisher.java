package com.bankflow.account.persistence.adapters;

import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.domain.models.Account;
import com.bankflow.account.persistence.jpa.OutboxJpaEntity;
import com.bankflow.account.persistence.jpa.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.AccountCreatedEvent;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;

@Component
public class OutboxAccountEventPublisher implements AccountEventPublisher {

    private final OutboxJpaRepository outboxRepository;
    private final JsonMapper jsonMapper;

    public OutboxAccountEventPublisher(OutboxJpaRepository outboxRepository, JsonMapper jsonMapper) {
        this.outboxRepository = outboxRepository;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void accountCreated(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getId(),
                account.getUserId(),
                account.getCurrency().name()
        );

        OutboxJpaEntity model = new OutboxJpaEntity();
        model.setAggregateId(account.getId());
        model.setEventType("AccountCreatedEvent");
        model.setPayload(jsonMapper.writeValueAsString(event));
        model.setStatus(OutboxStatus.PENDING);
        model.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(model);
    }
}