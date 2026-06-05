package com.bankflow.ledger.infra.adapters;

import com.bankflow.ledger.business.ports.IOutboxRepository;
import com.bankflow.ledger.core.entities.OutboxEntry;
import com.bankflow.ledger.infra.mappers.OutboxMapper;
import com.bankflow.ledger.infra.repositories.OutboxJpaRepository;
import com.bankflow.shared.enums.OutboxStatus;
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

    public List<OutboxEntry> findPending() {
        return outboxJpaRepository.findByStatus(OutboxStatus.PENDING)
                .stream()
                .map(outboxMapper::toDomain)
                .toList();
    }

    public void markAsProcessed(UUID id) {
        outboxJpaRepository.findById(id).ifPresent(model -> {
            model.setStatus(OutboxStatus.PROCESSED);
            model.setProcessedAt(LocalDateTime.now());
            outboxJpaRepository.save(model);
        });
    }
}