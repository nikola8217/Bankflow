package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.IEventStore;
import com.bankflow.transaction.core.aggregates.TransactionAggregate;
import com.bankflow.transaction.infra.mappers.TransactionEventMapper;
import com.bankflow.transaction.infra.repositories.TransactionEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class EventStoreAdapter implements IEventStore {

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
}