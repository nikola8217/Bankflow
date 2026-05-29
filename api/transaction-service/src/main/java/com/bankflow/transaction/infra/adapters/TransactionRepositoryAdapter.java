package com.bankflow.transaction.infra.adapters;

import com.bankflow.transaction.business.ports.ITransactionRepository;
import com.bankflow.transaction.core.entities.Transaction;
import com.bankflow.transaction.infra.mappers.TransactionMapper;
import com.bankflow.transaction.infra.repositories.TransactionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TransactionRepositoryAdapter implements ITransactionRepository {
    private final TransactionJpaRepository jpaRepository;
    private final TransactionMapper mapper;

    public TransactionRepositoryAdapter(TransactionJpaRepository jpaRepository, TransactionMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toModel(transaction)));
    }

    @Override
    public List<Transaction> findAllByAccountId(UUID accountId) {
        return jpaRepository.findAllByAccountId(accountId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
