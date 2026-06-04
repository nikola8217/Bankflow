package com.bankflow.ledger.infra.adapters;

import com.bankflow.ledger.business.ports.ITransactionHistoryRepository;
import com.bankflow.ledger.core.entities.TransactionHistory;
import com.bankflow.ledger.infra.mappers.TransactionHistoryMapper;
import com.bankflow.ledger.infra.repositories.TransactionHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TransactionHistoryRepositoryAdapter implements ITransactionHistoryRepository {

    private final TransactionHistoryJpaRepository jpaRepository;
    private final TransactionHistoryMapper mapper;

    @Override
    public void save(TransactionHistory history) {
        jpaRepository.save(mapper.toModel(history));
    }

    @Override
    public List<TransactionHistory> findAllByAccountId(UUID accountId) {
        return jpaRepository.findAllByAccountId(accountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}