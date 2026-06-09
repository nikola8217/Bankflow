package com.bankflow.ledger.infra.adapters;

import com.bankflow.ledger.infra.models.ProcessedEventModel;
import com.bankflow.ledger.infra.repositories.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter {

    private final ProcessedEventJpaRepository jpaRepository;

    public boolean exists(String key) {
        return jpaRepository.existsById(key);
    }

    public void save(String key) {
        ProcessedEventModel model = new ProcessedEventModel();
        model.setTransactionId(key);
        model.setProcessedAt(LocalDateTime.now());
        jpaRepository.save(model);
    }
}