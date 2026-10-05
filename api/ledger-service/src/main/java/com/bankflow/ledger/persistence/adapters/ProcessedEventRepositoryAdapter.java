package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.ProcessedEventRepository;
import com.bankflow.ledger.persistence.jpa.ProcessedEventJpaEntity;
import com.bankflow.ledger.persistence.jpa.repositories.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements ProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public boolean exists(String key) {
        return jpaRepository.existsById(key);
    }

    @Override
    public void save(String key) {
        ProcessedEventJpaEntity model = new ProcessedEventJpaEntity();
        model.setTransactionId(key);
        model.setProcessedAt(LocalDateTime.now());
        jpaRepository.save(model);
    }
}