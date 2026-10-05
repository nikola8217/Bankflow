package com.bankflow.transaction.persistence.adapters;

import com.bankflow.transaction.application.ports.OutboxRepository;
import com.bankflow.transaction.application.outbox.OutboxEntry;
import com.bankflow.transaction.persistence.mappers.OutboxMapper;
import com.bankflow.transaction.persistence.jpa.repositories.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;
    private final OutboxMapper outboxMapper;

    @Override
    public void save(OutboxEntry entry) {
        outboxJpaRepository.save(outboxMapper.toModel(entry));
    }
}