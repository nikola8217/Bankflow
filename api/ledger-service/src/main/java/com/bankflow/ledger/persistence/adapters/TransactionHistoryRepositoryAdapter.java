package com.bankflow.ledger.persistence.adapters;

import com.bankflow.ledger.application.ports.TransactionHistoryRepository;
import com.bankflow.ledger.domain.models.TransactionHistory;
import com.bankflow.ledger.persistence.mappers.TransactionHistoryMapper;
import com.bankflow.ledger.persistence.jpa.repositories.TransactionHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionHistoryRepositoryAdapter implements TransactionHistoryRepository {

    private final TransactionHistoryJpaRepository jpaRepository;
    private final TransactionHistoryMapper mapper;

    @Override
    public void save(TransactionHistory history) {
        jpaRepository.save(mapper.toModel(history));
    }

    @Override
    public List<TransactionHistory> findAllByAccountId(UUID accountId) {
        return jpaRepository.findStatement(accountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}