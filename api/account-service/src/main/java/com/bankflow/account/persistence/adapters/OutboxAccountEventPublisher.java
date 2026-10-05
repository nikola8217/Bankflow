package com.bankflow.account.persistence.adapters;

import com.bankflow.account.application.ports.AccountEventPublisher;
import com.bankflow.account.domain.models.Account;
import com.bankflow.account.persistence.jpa.OutboxJpaEntity;
import com.bankflow.account.persistence.jpa.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.shared.events.AccountCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OutboxAccountEventPublisher implements AccountEventPublisher {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final OutboxJpaRepository outboxRepository;

    public OutboxAccountEventPublisher(OutboxJpaRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
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
        model.setPayload(toJson(event));
        model.setStatus(OutboxStatus.PENDING);
        model.setCreatedAt(LocalDateTime.now());

        outboxRepository.save(model);
    }

    private String toJson(Object event) {
        try {
            return MAPPER.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize event", e);
        }
    }
}