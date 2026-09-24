package com.bankflow.ledger.infra.adapters;

import com.bankflow.ledger.business.ports.IProcessedEventRepository;
import com.bankflow.ledger.infra.models.ProcessedEventModel;
import com.bankflow.ledger.infra.repositories.ProcessedEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ProcessedEventRepositoryAdapter implements IProcessedEventRepository {

    private final ProcessedEventJpaRepository jpaRepository;

    @Override
    public boolean exists(String key) {
        return jpaRepository.existsById(key);
    }

    @Override
    public void save(String key) {
        ProcessedEventModel model = new ProcessedEventModel();
        model.setTransactionId(key);
        model.setProcessedAt(LocalDateTime.now());
        jpaRepository.save(model);
    }
}