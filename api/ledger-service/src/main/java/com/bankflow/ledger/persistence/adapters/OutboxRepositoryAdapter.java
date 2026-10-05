package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.OutboxRepository;
import com.bankflow.ledger.application.outbox.OutboxEntry;
import com.bankflow.ledger.persistence.mappers.OutboxMapper;
import com.bankflow.ledger.persistence.jpa.repositories.OutboxJpaRepository;
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