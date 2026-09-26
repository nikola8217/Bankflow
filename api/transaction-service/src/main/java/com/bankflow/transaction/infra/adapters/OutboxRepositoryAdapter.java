package com.bankflow.transaction.infra.adapters;

import com.bankflow.shared.enums.OutboxStatus;
import com.bankflow.transaction.business.ports.IOutboxRepository;
import com.bankflow.transaction.core.entities.OutboxEntry;
import com.bankflow.transaction.infra.mappers.OutboxMapper;
import com.bankflow.transaction.infra.repositories.OutboxJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxRepositoryAdapter implements IOutboxRepository {

    private final OutboxJpaRepository outboxJpaRepository;
    private final OutboxMapper outboxMapper;

    @Override
    public void save(OutboxEntry entry) {
        outboxJpaRepository.save(outboxMapper.toModel(entry));
    }
}