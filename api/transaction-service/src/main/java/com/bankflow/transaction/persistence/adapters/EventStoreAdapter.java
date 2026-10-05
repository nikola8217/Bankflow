package com.bankflow.transaction.persistence.adapters;

import com.bankflow.transaction.application.ports.EventStore;
import com.bankflow.transaction.domain.models.TransactionAggregate;
import com.bankflow.transaction.persistence.mappers.TransactionEventMapper;
import com.bankflow.transaction.persistence.jpa.TransactionEventJpaEntity;
import com.bankflow.transaction.persistence.jpa.repositories.TransactionEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventStoreAdapter implements EventStore {

    private final TransactionEventJpaRepository eventRepository;
    private final TransactionEventMapper eventMapper;

    @Override
    @Transactional
    public void save(TransactionAggregate aggregate) {
        List<Object> domainEvents = aggregate.pullDomainEvents();

        int version = aggregate.getVersion() - domainEvents.size() + 1;

        for (Object event : domainEvents) {
            eventRepository.save(eventMapper.toModel(event, aggregate.getTransactionId(), version++));
        }
    }

    @Override
    public Optional<TransactionAggregate> load(UUID transactionId) {
        List<TransactionEventJpaEntity> events = eventRepository
                .findByAggregateIdOrderByVersionAsc(transactionId);

        if (events.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(eventMapper.toAggregate(events));
    }
}